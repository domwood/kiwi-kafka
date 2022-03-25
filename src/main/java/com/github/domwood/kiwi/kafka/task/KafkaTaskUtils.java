package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.data.input.ConsumerStartPosition;
import com.github.domwood.kiwi.exceptions.ConsumerAssignmentTimeoutException;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.utils.KafkaConsumerTracker;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.kafka.utils.KafkaOffsetPositionCalculator.getStartingPositions;
import static java.time.temporal.ChronoUnit.MILLIS;

public class KafkaTaskUtils {

    private KafkaTaskUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(KafkaTaskUtils.class);
    private static final int POLL_INTERVAL = 10;
    private static final int MAX_POLL_COUNT = 500;

    public static KafkaConsumerTracker subscribeAndSeek(final KafkaConsumerResource<?, ?> resource,
                                                        final List<String> topics,
                                                        final Optional<ConsumerStartPosition> startPosition) {

        final Set<Integer> partitions = startPosition
                .map(ConsumerStartPosition::partitions)
                .orElse(Collections.emptySet());
        if (partitions.isEmpty()) {
            resource.subscribe(topics);
        } else {
            final Set<TopicPartition> topicPartitions = partitions.stream()
                    .flatMap(partition -> topics.stream().map(topic -> new TopicPartition(topic, partition)))
                    .collect(Collectors.toSet());
            resource.assign(topicPartitions);
        }

        logger.info("Consumer awaiting assignment for {} ...", topics);

        Set<TopicPartition> topicPartitionSet = resource.assignment();

        int pollCount = 0;
        while (topicPartitionSet.isEmpty() && pollCount++ < MAX_POLL_COUNT) {
            resource.poll(Duration.of(POLL_INTERVAL, MILLIS));
            topicPartitionSet = resource.assignment();
        }

        if (pollCount > MAX_POLL_COUNT && topicPartitionSet.isEmpty()) {
            throw new ConsumerAssignmentTimeoutException("Timed out awaiting an assignment for topics " + topics + " for " + resource.getGroupId());
        }

        logger.info("Consumer attained assignment {} for {}. Seeking to beginning ...", topicPartitionSet, topics);

        resource.seekToBeginning(topicPartitionSet);

        Map<TopicPartition, Long> endOffsets = resource.endOffsets(topicPartitionSet);
        Map<TopicPartition, Long> startOffset = resource.currentPosition(endOffsets.keySet());
        Map<TopicPartition, Long> consumerStartingPosition = startPosition
                .map(position -> getStartingPositions(startOffset, endOffsets, position))
                .orElse(startOffset);

        startPosition.ifPresent(s -> {
            logger.info("Consumer start position defined, scanning to the partition start");
            resource.seek(consumerStartingPosition);
        });

        logger.info("Consumer sought to beginning, polling for records");

        return new KafkaConsumerTracker(startOffset, endOffsets, consumerStartingPosition);
    }

    public static String formatCoordinator(Node coordinator) {
        return String.format("%s (%s:%s)", coordinator.id(), coordinator.host(), coordinator.port());
    }
}
