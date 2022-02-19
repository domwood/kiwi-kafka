package com.github.domwood.kiwi.kafka.task.consumer;


import com.github.domwood.kiwi.data.input.AbstractConsumerRequest;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.filters.FilterBuilder;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.FuturisingAbstractKafkaTask;
import com.github.domwood.kiwi.kafka.task.KafkaContinuousTask;
import com.github.domwood.kiwi.kafka.task.KafkaTaskUtils;
import com.github.domwood.kiwi.kafka.utils.KafkaConsumerTracker;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.fromKafkaHeaders;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ContinuousConsumeMessages<K, V>
        extends FuturisingAbstractKafkaTask<AbstractConsumerRequest, Void, KafkaConsumerResource<K, V>>
        implements KafkaContinuousTask<AbstractConsumerRequest, ConsumerResponse> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Integer BATCH_SIZE = 100;
    private static final Integer MAX_MESSAGES = 500;
    private static final Integer MAX_MESSAGE_BYTES = 500 * 2000 * 16;

    private final AtomicBoolean closed;
    private final AtomicBoolean paused;

    private Consumer<ConsumerResponse> consumer;
    private final List<MessageFilter> filters;
    private final Map<TopicPartition, Long> currentPosition;

    public ContinuousConsumeMessages(final KafkaConsumerResource<K, V> resource,
                                     final AbstractConsumerRequest input) {
        super(resource, input);

        this.consumer = message -> logger.warn("No consumer attached to kafka task");
        this.paused = new AtomicBoolean(false);
        this.closed = new AtomicBoolean(false);
        this.filters = new ArrayList<>(input.filters());
        this.currentPosition = new HashMap<>();
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
    public void unpause() {
        this.paused.set(false);
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

    @Override
    public void update(AbstractConsumerRequest input) {
        this.filters.clear();
        this.filters.addAll(input.filters());
    }

    @Override
    public void registerConsumer(Consumer<ConsumerResponse> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected Void delegateExecuteSync() {

        try {
            KafkaConsumerTracker tracker = KafkaTaskUtils.subscribeAndSeek(resource, input.topics(), input.consumerStartPosition(), input.filters());
            Pair<Map<TopicPartition, Long>, ConsumerPosition> consumerPosition = tracker.gatherUpdatedPosition(resource);
            forward(emptyList(), consumerPosition.getRight());
            this.currentPosition.putAll(consumerPosition.getLeft());

            boolean wasPaused = false;
            int idleCount = 0;
            while (!this.isClosed()) {
                if (this.paused.get()) {
                    MILLISECONDS.sleep(20);
                    wasPaused = true;
                } else {
                    ConsumerRecords<K, V> records = resource.poll(Duration.of(Integer.max(10 ^ (idleCount + 1), 5000), MILLIS));

                    if (wasPaused) pauseCheck(tracker.assignedPartitions());
                    wasPaused = false;

                    if (records.isEmpty()) {
                        idleCount++;
                        logger.debug("No records polled for topic {} ", input.topics());
                        forward(emptyList(), tracker.gatherUpdatedPosition(resource).getRight());
                    } else {
                        idleCount = 0;
                        Predicate<ConsumerRecord<K, V>> filter = FilterBuilder.compileFilters(this.filters, resource::convertKafkaKey, resource::convertKafkaValue);
                        ArrayList<ConsumedMessage> messages = new ArrayList<>(BATCH_SIZE);

                        Iterator<ConsumerRecord<K, V>> recordIterator = records.iterator();
                        Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();
                        int totalBatchSize = 0;
                        boolean commitAfterEndOfPoll = true;
                        while (recordIterator.hasNext() && !this.isClosed()) {
                            commitAfterEndOfPoll = true;
                            ConsumerRecord<K, V> kafkaRecord = recordIterator.next();
                            tracker.incrementRecordCount();

                            if (filter.test(kafkaRecord)) {
                                ConsumedMessage consumedMessage = asConsumedRecord(kafkaRecord, resource::convertKafkaKey, resource::convertKafkaValue);
                                messages.add(consumedMessage);
                                toCommit.put(new TopicPartition(kafkaRecord.topic(), kafkaRecord.partition()), new OffsetAndMetadata(kafkaRecord.offset()));
                                totalBatchSize += Optional.ofNullable(consumedMessage.message()).orElse("").length() * 16;
                            }

                            if (totalBatchSize >= MAX_MESSAGE_BYTES || messages.size() >= MAX_MESSAGES) {
                                forwardAndMaybeCommit(resource, messages, toCommit, tracker);
                                totalBatchSize = 0;
                                commitAfterEndOfPoll = false;
                            }
                        }
                        if (commitAfterEndOfPoll) {
                            forwardAndMaybeCommit(resource, messages, toCommit, tracker);
                        }
                    }
                }
            }

            this.resource.unsubscribe();
        } catch (Exception e) {
            logger.error("Error occurred during continuous kafka consuming", e);
        }
        logger.info("Task has completed");
        return null;
    }

    private void logCommit(Map<TopicPartition, OffsetAndMetadata> offsetData, Exception exception) {
        if (exception != null) {
            logger.error("Failed to commit offset ", exception);
        } else {
            logger.debug("Commit offset data {}", offsetData);
        }
    }

    private ConsumedMessage asConsumedRecord(ConsumerRecord<K, V> consumerRecord, Function<K, String> keyFn, Function<V, String> valueFn) {
        return ImmutableConsumedMessage.builder()
                .timestamp(consumerRecord.timestamp())
                .offset(consumerRecord.offset())
                .partition(consumerRecord.partition())
                .key(keyFn.apply(consumerRecord.key()))
                .message(valueFn.apply(consumerRecord.value()))
                .headers(fromKafkaHeaders(consumerRecord.headers()))
                .build();
    }

    private void forwardAndMaybeCommit(KafkaConsumerResource<K, V> resource,
                                       List<ConsumedMessage> messages,
                                       Map<TopicPartition, OffsetAndMetadata> toCommit,
                                       KafkaConsumerTracker tracker) {
        //Blocking Call
        logger.info("Message batch size {} forwarding to consumers", messages.size());

        if (!this.isClosed()) {
            Pair<Map<TopicPartition, Long>, ConsumerPosition> consumerPosition = tracker.gatherUpdatedPosition(resource);
            this.currentPosition.putAll(consumerPosition.getLeft());
            forward(messages, consumerPosition.getRight());

            if (resource.isCommittingConsumer()) {
                resource.commitAsync(toCommit, this::logCommit);
            }

            messages.clear();
            toCommit.clear();
        }
    }

    private void pauseCheck(Set<TopicPartition> topicPartitions) {
        Map<TopicPartition, Long> latestPosition = resource.currentPosition(topicPartitions);
        boolean positionChanged = latestPosition.keySet().stream()
                .anyMatch(aLong -> !Objects.equals(currentPosition.get(aLong), latestPosition.get(aLong)));
        if (positionChanged) {
            logger.debug("Position has changed since pause, seeking back to original position");
            resource.seek(currentPosition);
        }
    }

    private void forward(List<ConsumedMessage> messages,
                         ConsumerPosition position) {
        if (!this.isClosed()) {
            this.consumer.accept(ImmutableConsumerResponse.builder()
                    .messages(messages)
                    .position(position)
                    .build());
        }
    }

}
