package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.data.input.ConsumerStartPosition;
import com.github.domwood.kiwi.data.input.filter.FilterApplication;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.exceptions.ConsumerAssignmentTimeoutException;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.utils.KafkaConsumerTracker;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.kafka.utils.KafkaOffsetPositionCalculator.getStartingPositions;
import static com.github.domwood.kiwi.utilities.NumberUtils.safeInt;
import static java.time.temporal.ChronoUnit.MILLIS;

public class KafkaTaskUtils {

    private KafkaTaskUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(KafkaTaskUtils.class);
    private static final int POLL_INTERVAL = 10;
    private static final int MAX_POLL_COUNT = 500;

    public static KafkaConsumerTracker subscribeAndSeek(final KafkaConsumerResource<?, ?> resource,
                                                        final List<String> topics,
                                                        final Optional<ConsumerStartPosition> startPostition,
                                                        final List<MessageFilter> filters) {

        resource.subscribe(topics);

        logger.info("Consumer awaiting assignment for {} ...", topics);

        Set<TopicPartition> topicPartitionSet = getAssignment(resource, topics);

        Set<TopicPartition> filterPartitions = applyFiltersForOffset(topicPartitionSet, filters);
        if (filterPartitions != topicPartitionSet && !filterPartitions.isEmpty()) {
            logger.info("Partition filters present, updating assignment...");
            resource.unsubscribe();
            resource.assign(filterPartitions);
            topicPartitionSet = getAssignment(resource, topics);
        }

        logger.info("Consumer attained assignment {} for {}. Seeking to beginning ...", topicPartitionSet, topics);

        resource.seekToBeginning(topicPartitionSet);

        Map<TopicPartition, Long> endOffsets = resource.endOffsets(topicPartitionSet);
        Map<TopicPartition, Long> startOffset = resource.currentPosition(endOffsets.keySet());
        Map<TopicPartition, Long> consumerStartingPosition = startPostition
                .map(position -> getStartingPositions(startOffset, endOffsets, position))
                .orElse(startOffset);

        startPostition.ifPresent(s -> {
            logger.info("Consumer start position defined, scanning to the partition start");
            resource.seek(consumerStartingPosition);
        });

        logger.info("Consumer sought to beginning, polling for records");

        return new KafkaConsumerTracker(startOffset, endOffsets, consumerStartingPosition);
    }

    private static Set<TopicPartition> getAssignment(final KafkaConsumerResource<?, ?> resource,
                                                     final List<String> topics) {
        Set<TopicPartition> topicPartitionSet = resource.assignment();

        int pollCount = 0;
        while (topicPartitionSet.isEmpty() && pollCount++ < MAX_POLL_COUNT) {
            resource.poll(Duration.of(POLL_INTERVAL, MILLIS));
            topicPartitionSet = resource.assignment();
        }

        if (pollCount > MAX_POLL_COUNT && topicPartitionSet.isEmpty()) {
            throw new ConsumerAssignmentTimeoutException("Timed out awaiting an assignment for topics " + topics + " for " + resource.getGroupId());
        }

        logger.info("Consumer assignment retrieved {}", topicPartitionSet);
        return topicPartitionSet;
    }

    public static String formatCoordinator(Node coordinator) {
        return String.format("%s (%s:%s)", coordinator.id(), coordinator.host(), coordinator.port());
    }

    private static Set<TopicPartition> applyFiltersForOffset(final Set<TopicPartition> topicPartitions,
                                                             final List<MessageFilter> filters) {
        return topicPartitions.stream()
                .filter(topic -> filters.stream().allMatch(filter -> filterPartition(topic, filter)))
                .collect(Collectors.toSet());
    }

    private static boolean filterPartition(final TopicPartition topic, final MessageFilter filter) {
        if (filter.filterApplication() != FilterApplication.PARTITION) {
            return true;
        } else {
            switch (filter.filterType()) {
                case GREATER_THAN:
                    return topic.partition() > safeInt(filter.filter());
                case LESS_THAN:
                    return topic.partition() < safeInt(filter.filter());
                case MATCHES:
                    return topic.partition() == safeInt(filter.filter());
                case NOT_MATCHES:
                    return topic.partition() != safeInt(filter.filter());
                default:
                    return true;
            }
        }
    }

}
