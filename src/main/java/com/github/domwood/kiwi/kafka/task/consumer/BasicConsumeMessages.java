package com.github.domwood.kiwi.kafka.task.consumer;

import com.github.domwood.kiwi.data.input.AbstractConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableConsumedMessage;
import com.github.domwood.kiwi.data.output.ImmutableConsumerResponse;
import com.github.domwood.kiwi.kafka.filters.FilterBuilder;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.FuturisingAbstractKafkaTask;
import com.github.domwood.kiwi.kafka.task.KafkaTaskUtils;
import com.github.domwood.kiwi.kafka.utils.KafkaConsumerTracker;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.fromKafkaHeaders;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toList;


public class BasicConsumeMessages<K, V> extends FuturisingAbstractKafkaTask<AbstractConsumerRequest, ConsumerResponse, KafkaConsumerResource<K, V>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BasicConsumeMessages(KafkaConsumerResource<K, V> resource, AbstractConsumerRequest input) {
        super(resource, input);
    }

    @Override
    protected ConsumerResponse delegateExecuteSync() {

        try {
            KafkaConsumerTracker tracker = KafkaTaskUtils.subscribeAndSeek(resource, input.topics(), input.consumerStartPosition(), input.filters());

            Queue<ConsumedMessage> queue = selectQueueType();

            boolean running = true;
            int pollEmptyCount = 0;
            Predicate<ConsumerRecord<K, V>> filter = FilterBuilder.compileFilters(input.filters(), resource::convertKafkaKey, resource::convertKafkaValue);

            while (running) {
                ConsumerRecords<K, V> records = resource.poll(Duration.of(200, MILLIS));
                Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();

                if (records.isEmpty()) {
                    logger.debug("No records polled updating empty count");
                    pollEmptyCount++;
                } else {
                    logger.debug("Polled {} messages from {} topic ", records.count(), input.topics());

                    pollEmptyCount = 0;
                    for (ConsumerRecord<K, V> consumerRecord : records) {
                        if (filter.test(consumerRecord)) {
                            queue.add(asConsumedRecord(consumerRecord, resource::convertKafkaKey, resource::convertKafkaValue));
                            toCommit.put(new TopicPartition(consumerRecord.topic(), consumerRecord.partition()), new OffsetAndMetadata(consumerRecord.offset()));
                        }
                    }
                    commitAsync(toCommit);
                }
                running = shouldContinueRunning(pollEmptyCount, tracker.getEndOffsets(), toCommit);
            }
            this.resource.unsubscribe();

            return ImmutableConsumerResponse.builder()
                    .messages(queue.stream()
                            .sorted(Comparator.comparingLong(ConsumedMessage::timestamp))
                            .collect(toList()))
                    .build();
        } catch (Exception e) {
            logger.error("Failed to complete task of consuming from topics " + input.topics(), e);
            throw e;
        }
    }

    private boolean shouldContinueRunning(int pollEmptyCount,
                                          Map<TopicPartition, Long> endOffsets,
                                          Map<TopicPartition, OffsetAndMetadata> toCommit) {

        if (pollEmptyCount > 3) {
            logger.debug("Polled empty 3 times, closing consumer");
            return false;
        }
        if (isEndOfData(endOffsets, toCommit)) {
            logger.debug("End of data reached");
            return false;
        }

        return true;
    }


    private Queue<ConsumedMessage> selectQueueType() {
        return new CircularFifoQueue<>(input.limit());
    }

    private void commitAsync(Map<TopicPartition, OffsetAndMetadata> toCommit) {
        if (resource.isCommittingConsumer() && !toCommit.isEmpty()) {
            resource.commitAsync(toCommit, this::logCommit);
        }
    }

    private void logCommit(Map<TopicPartition, OffsetAndMetadata> offsetData, Exception exception) {
        if (exception != null) {
            logger.error("Failed to commit offset ", exception);
        } else {
            logger.debug("Commit offset data {}", offsetData);
        }
    }

    private boolean isEndOfData(Map<TopicPartition, Long> endOffsets, Map<TopicPartition, OffsetAndMetadata> lastCommit) {
        return endOffsets.entrySet().stream()
                .allMatch(kv -> {
                    if (kv.getValue() < 1) {
                        return true;
                    } else {
                        OffsetAndMetadata latestCommit = lastCommit.get(kv.getKey());
                        return latestCommit != null && latestCommit.offset() + 1 >= kv.getValue();
                    }
                });
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

}
