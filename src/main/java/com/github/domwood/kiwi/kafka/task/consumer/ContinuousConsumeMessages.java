package com.github.domwood.kiwi.kafka.task.consumer;


import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.filters.FilterBuilder;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.KafkaContinuousTask;
import com.github.domwood.kiwi.utilities.FutureUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.fromKafkaHeaders;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;

public class ContinuousConsumeMessages implements KafkaContinuousTask<ConsumerRequest, KafkaConsumerResource<String, String>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Integer batchSize = 50;
    private final AtomicBoolean closeTask;
    private final AtomicBoolean closed;
    private final AtomicBoolean paused;

    private Consumer<OutboundResponse> consumer;
    private volatile List<MessageFilter> filters;

    public ContinuousConsumeMessages(){
        this.consumer = (message) -> logger.warn("No consumer attached to kafka task");
        this.closeTask = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.closed = new AtomicBoolean(false);
        this.filters = emptyList();
    }

    @Override
    public void close() {
        this.closeTask.set(true);
    }

    @Override
    public void pause() {
        this.paused.set(true);
    }

    @Override
    public void update(ConsumerRequest input) {
        this.filters = input.filters();
    }

    @Override
    public void registerConsumer(Consumer<OutboundResponse> consumer) {
        this.consumer = consumer;
    }

    @Override
    public CompletableFuture<Void> execute(KafkaConsumerResource<String, String> resource, ConsumerRequest input) {
        this.filters = input.filters();

        return FutureUtils.supplyAsync(() -> {
            try{
                ConsumerUtils.subscribeAndSeek(resource, input);

                boolean running = true;
                while(running) {
                    if(this.paused.get()){
                        Thread.sleep(20);
                        resource.keepAlive();
                    }
                    else if(this.closeTask.get()){
                        running = false;
                        this.closed.set(true);
                        logger.info("Task set to close, closing...");
                    }
                    else{
                        ConsumerRecords<String, String> records = resource.poll(Duration.of(10, MILLIS));
                        if (records.isEmpty()) {
                            logger.debug("No records polled for topic {} ", input.topics());
                        } else {
                            Predicate<ConsumerRecord<String, String>> filter = FilterBuilder.compileFilters(this.filters);
                            ArrayList<ConsumedMessage<String, String>> messages = new ArrayList<>(batchSize);

                            Iterator<ConsumerRecord<String, String>> recordIterator = records.iterator();
                            Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();
                            while (recordIterator.hasNext()) {
                                ConsumerRecord<String, String> record = recordIterator.next();
                                String payload = record.value();
                                if (filter.test(record)) {
                                    ConsumedMessage<String, String> message = ImmutableConsumedMessage.<String, String>builder()
                                            .timestamp(record.timestamp())
                                            .offset(record.offset())
                                            .partition(record.partition())
                                            .key(record.key())
                                            .message(payload)
                                            .headers(fromKafkaHeaders(record.headers()))
                                            .build();

                                    messages.add(message);
                                    toCommit.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                                }

                                if (messages.size() >= batchSize) {
                                    ConsumerResponse<String, String> response = ImmutableConsumerResponse.<String, String>builder()
                                            .messages(messages)
                                            .build();
                                    //Blocking Call
                                    this.consumer.accept(response);

                                    resource.commitAsync(toCommit, this::logCommit);
                                    messages.clear();
                                    toCommit.clear();

                                    resource.keepAlive();
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e){
                logger.error("Error occurred during continuous kafka consuming", e);
            }
            finally {
                resource.discard();
            }
            return null;
        });
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

    private void logCommit(Map<TopicPartition, OffsetAndMetadata> offsetData, Exception exception){
        if(exception != null){
            logger.error("Failed to commit offset ", exception);
        }
        else{
            logger.debug("Commit offset data {}", offsetData);
        }
    }
}
