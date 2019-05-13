package com.github.domwood.kiwi.kafka.task.consumer;


import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import com.github.domwood.kiwi.data.output.ImmutableConsumedMessage;
import com.github.domwood.kiwi.data.output.ImmutableConsumerResponse;
import com.github.domwood.kiwi.data.output.OutboundResponse;
import com.github.domwood.kiwi.kafka.filters.FilterBuilder;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.KafkaContinuousTask;
import com.github.domwood.kiwi.kafka.task.KafkaTaskUtils;
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
                KafkaTaskUtils.subscribeAndSeek(resource, input.topics(), true);

                boolean running = true;
                int idleCount = 0;
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
                        ConsumerRecords<String, String> records = resource.poll(Duration.of(Integer.max(10^(idleCount+1), 5000), MILLIS));
                        if (records.isEmpty()) {
                            idleCount++;
                            logger.debug("No records polled for topic {} ", input.topics());
                            resource.keepAlive();

                        } else {
                            idleCount = 0;
                            Predicate<ConsumerRecord<String, String>> filter = FilterBuilder.compileFilters(this.filters);
                            ArrayList<ConsumedMessage<String, String>> messages = new ArrayList<>(batchSize);

                            Iterator<ConsumerRecord<String, String>> recordIterator = records.iterator();
                            Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();
                            while (recordIterator.hasNext()) {
                                ConsumerRecord<String, String> record = recordIterator.next();

                                if (filter.test(record)) {
                                    messages.add(asConsumedRecord(record));
                                    toCommit.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                                }

                                if (messages.size() >= batchSize) {
                                    forwardAndCommit(resource, messages, toCommit);
                                }
                            }
                            if(!messages.isEmpty()){
                                forwardAndCommit(resource, messages, toCommit);
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

    private void logCommit(Map<TopicPartition, OffsetAndMetadata> offsetData, Exception exception){
        if(exception != null){
            logger.error("Failed to commit offset ", exception);
        }
        else{
            logger.debug("Commit offset data {}", offsetData);
        }
    }

    private ConsumedMessage<String, String> asConsumedRecord(ConsumerRecord<String, String> record){
        return ImmutableConsumedMessage.<String, String>builder()
                .timestamp(record.timestamp())
                .offset(record.offset())
                .partition(record.partition())
                .key(record.key())
                .message(record.value())
                .headers(fromKafkaHeaders(record.headers()))
                .build();
    }

    private void forwardAndCommit(KafkaConsumerResource<String, String> resource,
                                 List<ConsumedMessage<String, String>> messages,
                                 Map<TopicPartition, OffsetAndMetadata> toCommit){
        //Blocking Call
        logger.info("Message batch size forwarding to consumers");

        this.consumer.accept(ImmutableConsumerResponse.<String, String>builder()
                .messages(messages)
                .build());

        resource.commitAsync(toCommit, this::logCommit);

        messages.clear();
        toCommit.clear();

        resource.keepAlive();
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

}
