package com.github.domwood.kiwi.kafka.task.consumer;


import com.github.domwood.kiwi.data.input.AbstractConsumerRequest;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import com.github.domwood.kiwi.data.output.ConsumerPosition;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableConsumedMessage;
import com.github.domwood.kiwi.data.output.ImmutableConsumerResponse;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private volatile List<MessageFilter> filters;

    public ContinuousConsumeMessages(KafkaConsumerResource<K, V> resource,
                                     AbstractConsumerRequest input) {
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
    public void update(AbstractConsumerRequest input) {
        this.filters = input.filters();
    }

    @Override
    public void registerConsumer(Consumer<ConsumerResponse> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected Void delegateExecuteSync() {
        this.filters = input.filters();

        try {
            KafkaConsumerTracker tracker = KafkaTaskUtils.subscribeAndSeek(resource, input.topics(), input.consumerStartPosition());

            forward(emptyList(), tracker.position(resource));

            int idleCount = 0;
            while (!this.isClosed()) {
                if (this.paused.get()) {
                    MILLISECONDS.sleep(20);
                } else {
                    ConsumerRecords<K, V> records = resource.poll(Duration.of(Integer.max(10 ^ (idleCount + 1), 5000), MILLIS));
                    if (records.isEmpty()) {
                        idleCount++;
                        logger.debug("No records polled for topic {} ", input.topics());
                        forward(emptyList(), tracker.position(resource));

                    } else {
                        idleCount = 0;
                        Predicate<ConsumerRecord<K, V>> filter = FilterBuilder.compileFilters(this.filters, resource::convertKafkaKey, resource::convertKafkaValue);
                        ArrayList<ConsumedMessage> messages = new ArrayList<>(BATCH_SIZE);

                        Iterator<ConsumerRecord<K, V>> recordIterator = records.iterator();
                        Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();
                        int totalBatchSize = 0;
                        while (recordIterator.hasNext() && !this.isClosed()) {
                            ConsumerRecord<K, V> record = recordIterator.next();
                            tracker.incrementRecordCount();

                            if (filter.test(record)) {
                                ConsumedMessage consumedMessage = asConsumedRecord(record, resource::convertKafkaKey, resource::convertKafkaValue);
                                messages.add(consumedMessage);
                                toCommit.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                                totalBatchSize += Optional.ofNullable(consumedMessage.message()).orElse("").length() * 16;
                            }

                            if (totalBatchSize >= MAX_MESSAGE_BYTES || messages.size() >= MAX_MESSAGES) {
                                forwardAndMaybeCommit(resource, messages, toCommit, tracker.position(resource));
                                totalBatchSize = 0;
                            }
                        }
                        forwardAndMaybeCommit(resource, messages, toCommit, tracker.position(resource));
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
                                       ConsumerPosition position) {
        //Blocking Call
        logger.info("Message batch size {} forwarding to consumers", messages.size());

        if (!this.isClosed()) {
            forward(messages, position);

            if (resource.isCommittingConsumer()) {
                resource.commitAsync(toCommit, this::logCommit);
            }

            messages.clear();
            toCommit.clear();
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

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

}
