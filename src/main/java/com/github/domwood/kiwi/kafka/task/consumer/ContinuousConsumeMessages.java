package com.github.domwood.kiwi.kafka.task.consumer;


import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.filters.FilterBuilder;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.FuturisingAbstractKafkaTask;
import com.github.domwood.kiwi.kafka.task.KafkaContinuousTask;
import com.github.domwood.kiwi.kafka.task.KafkaTaskUtils;
import com.github.domwood.kiwi.kafka.utils.KafkaConsumerTracker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.fromKafkaHeaders;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;

public class ContinuousConsumeMessages extends FuturisingAbstractKafkaTask<ConsumerRequest, Void, KafkaConsumerResource<String, String>> implements KafkaContinuousTask<ConsumerRequest>{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Integer BATCH_SIZE = 100;
    private final AtomicBoolean closed;
    private final AtomicBoolean paused;

    private Consumer<OutboundResponseWithPosition> consumer;
    private volatile List<MessageFilter> filters;

    public ContinuousConsumeMessages(KafkaConsumerResource<String, String> resource,
                                     ConsumerRequest input){
        super(resource, input);

        this.consumer = message -> logger.warn("No consumer attached to kafka task");
        this.paused = new AtomicBoolean(false);
        this.closed = new AtomicBoolean(false);
        this.filters = emptyList();
    }

    @Override
    public void close() {
        logger.info("Task set to close, closing...");
        this.closed.set(true);
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
    public void registerConsumer(Consumer<OutboundResponseWithPosition> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected Void delegateExecuteSync() {
        this.filters = input.filters();

        try{
            Map<TopicPartition, Long> endOffsets = KafkaTaskUtils.subscribeAndSeek(resource, input.topics(), true);
            Map<TopicPartition, Long> startOffsets = resource.currentPosition(endOffsets.keySet());
            KafkaConsumerTracker tracker = new KafkaConsumerTracker(endOffsets,startOffsets);

            forward(emptyList(), tracker.position(resource));

            int idleCount = 0;
            while(!this.isClosed()) {
                if(this.paused.get()){
                    Thread.sleep(20);
                    resource.keepAlive();
                }
                else{
                    ConsumerRecords<String, String> records = resource.poll(Duration.of(Integer.max(10^(idleCount+1), 5000), MILLIS));
                    if (records.isEmpty()) {
                        idleCount++;
                        logger.debug("No records polled for topic {} ", input.topics());
                        resource.keepAlive();
                        forward(emptyList(), tracker.position(resource));

                    } else {
                        idleCount = 0;
                        Predicate<ConsumerRecord<String, String>> filter = FilterBuilder.compileFilters(this.filters);
                        ArrayList<ConsumedMessage<String, String>> messages = new ArrayList<>(BATCH_SIZE);

                        Iterator<ConsumerRecord<String, String>> recordIterator = records.iterator();
                        Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();
                        while (recordIterator.hasNext() && !this.isClosed()) {
                            ConsumerRecord<String, String> record = recordIterator.next();
                            tracker.incrementRecordCount();

                            if (filter.test(record)) {
                                messages.add(asConsumedRecord(record));
                                toCommit.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                            }

                            if (messages.size() >= BATCH_SIZE) {
                                forwardAndCommit(resource, messages, toCommit, tracker.position(resource));
                            }
                        }
                        forwardAndCommit(resource, messages, toCommit, tracker.position(resource));
                    }
                }
            }
        }
        catch (Exception e){
            logger.error("Error occurred during continuous kafka consuming", e);
        }
        logger.info("Task has completed");
        return null;
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
                                  Map<TopicPartition, OffsetAndMetadata> toCommit,
                                  ConsumerPosition position){
        //Blocking Call
        logger.info("Message batch size {} forwarding to consumers", messages.size());

        if(!this.isClosed()){
            forward(messages, position);

            resource.commitAsync(toCommit, this::logCommit);

            messages.clear();
            toCommit.clear();

            resource.keepAlive();
        }
    }

    private void forward(List<ConsumedMessage<String, String>> messages,
                         ConsumerPosition position){
        if(!this.isClosed()){
            this.consumer.accept(ImmutableConsumerResponse.<String, String>builder()
                    .messages(messages)
                    .position(position)
                    .build());
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

}
