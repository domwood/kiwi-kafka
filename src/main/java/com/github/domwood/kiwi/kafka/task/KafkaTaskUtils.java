package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.kafka.exceptions.ConsumerAssignmentTimeoutException;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MILLIS;

public interface KafkaTaskUtils {

    Logger logger = LoggerFactory.getLogger(KafkaTaskUtils.class);
    int pollInterval = 10;
    int maxPollCount = 500;

    static Map<TopicPartition, Long> subscribeAndSeek(KafkaConsumerResource<?, ?> resource,
                                                      List<String> topics,
                                                      boolean seekToBeginning) {
        resource.subscribe(topics);

        logger.debug("Consumer awaiting assignment for {} ...", topics);

        Set<org.apache.kafka.common.TopicPartition> topicPartitionSet = resource.assignment();

        int pollCount = 0;
        while (topicPartitionSet.isEmpty() && pollCount++ < maxPollCount) {
            resource.poll(Duration.of(pollInterval, MILLIS));
            topicPartitionSet = resource.assignment();
        }

        if (pollCount > maxPollCount && topicPartitionSet.isEmpty()) {
            throw new ConsumerAssignmentTimeoutException("Timed out awaiting an assignment for topics " + topics);
        }

        if (seekToBeginning) {
            logger.debug("Consumer attained assignment {} for {}. Seeking to beginning ...", topicPartitionSet, topics);

            resource.seekToBeginning(topicPartitionSet);

            logger.debug("Consumer sought to beginning, polling for records");
        }

        Map<TopicPartition, java.lang.Long> endOffsets = resource.endOffsets(topicPartitionSet);

        return endOffsets;
    }
}
