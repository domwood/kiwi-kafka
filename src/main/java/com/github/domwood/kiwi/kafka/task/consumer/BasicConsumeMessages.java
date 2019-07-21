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
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.fromKafkaHeaders;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toList;


public class BasicConsumeMessages extends FuturisingAbstractKafkaTask<AbstractConsumerRequest, ConsumerResponse<String, String>, KafkaConsumerResource<String, String>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BasicConsumeMessages(KafkaConsumerResource<String, String> resource, AbstractConsumerRequest input) {
        super(resource, input);
    }

    @Override
    protected ConsumerResponse<String, String> delegateExecuteSync() {

        try {
            Map<TopicPartition, Long> endOffsets = KafkaTaskUtils.subscribeAndSeek(resource, input.topics(), true);

            Queue<ConsumedMessage<String, String>> queue = selectQueueType();

            boolean running = true;
            int pollEmptyCount = 0;
            Predicate<ConsumerRecord<String, String>> filter = FilterBuilder.compileFilters(input.filters());

            while (running) {
                ConsumerRecords<String, String> records = resource.poll(Duration.of(200, MILLIS));
                Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();

                if (records.isEmpty()) {
                    logger.debug("No records polled updating empty count");
                    pollEmptyCount++;
                } else {
                    logger.debug("Polled {} messages from {} topic ", records.count(), input.topics());

                    pollEmptyCount = 0;
                    Iterator<ConsumerRecord<String, String>> recordIterator = records.iterator();
                    while (recordIterator.hasNext() && !inputLimitReached(queue)) {
                        ConsumerRecord<String, String> record = recordIterator.next();
                        if (filter.test(record)) {
                            queue.add(asConsumedRecord(record));
                            toCommit.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                        }
                    }
                    commitAsync(toCommit);
                }
                running = shouldContinueRunning(pollEmptyCount, endOffsets, toCommit, queue);
            }

            return ImmutableConsumerResponse.<String, String>builder()
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
                                          Map<TopicPartition, OffsetAndMetadata> toCommit,
                                          Queue<ConsumedMessage<String, String>> queue) {

        if (pollEmptyCount > 3) {
            logger.debug("Polled empty 3 times, closing consumer");
            return false;
        }
        if (inputLimitReached(queue)) {
            logger.debug("Max queue size reached");
            return false;
        }
        if (isEndOfData(endOffsets, toCommit)) {
            logger.debug("End of data reached");
            return false;
        }

        return true;
    }

    private boolean inputLimitReached(Queue<ConsumedMessage<String, String>> queue) {
        if(input.limit() < 1){
            return false;
        }
        if(input.limitAppliesFromStart()){
            return queue.size() >= input.limit();
        }
        return false;
    }

    private Queue<ConsumedMessage<String, String>> selectQueueType() {
        if (input.limit() > 0 && !input.limitAppliesFromStart()) {
            return new CircularFifoQueue<>(input.limit());
        } else {
            return new LinkedList<>();
        }
    }

    private void commitAsync(Map<TopicPartition, OffsetAndMetadata> toCommit) {
        if (!toCommit.isEmpty()) {
            resource.commitAsync(toCommit, this::logCommit);
            resource.keepAlive();
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

    private ConsumedMessage<String, String> asConsumedRecord(ConsumerRecord<String, String> record) {
        return ImmutableConsumedMessage.<String, String>builder()
                .timestamp(record.timestamp())
                .offset(record.offset())
                .partition(record.partition())
                .key(record.key())
                .message(record.value())
                .headers(fromKafkaHeaders(record.headers()))
                .build();
    }

}
