package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.exceptions.ConsumerAssignmentTimeoutException;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MILLIS;

public class KafkaTaskUtils {

    private KafkaTaskUtils(){}

    private static final Logger logger = LoggerFactory.getLogger(KafkaTaskUtils.class);
    private static final int POLL_INTERVAL = 10;
    private static final int MAX_POLL_COUNT = 500;

    public static Map<TopicPartition, Long> subscribeAndSeek(KafkaConsumerResource<?, ?> resource,
                                                      List<String> topics,
                                                      boolean seekToBeginning) {
        resource.subscribe(topics);

        logger.debug("Consumer awaiting assignment for {} ...", topics);

        Set<org.apache.kafka.common.TopicPartition> topicPartitionSet = resource.assignment();

        int pollCount = 0;
        while (topicPartitionSet.isEmpty() && pollCount++ < MAX_POLL_COUNT) {
            resource.poll(Duration.of(POLL_INTERVAL, MILLIS));
            topicPartitionSet = resource.assignment();
        }

        if (pollCount > MAX_POLL_COUNT && topicPartitionSet.isEmpty()) {
            throw new ConsumerAssignmentTimeoutException("Timed out awaiting an assignment for topics " + topics);
        }

        if (seekToBeginning) {
            logger.debug("Consumer attained assignment {} for {}. Seeking to beginning ...", topicPartitionSet, topics);

            resource.seekToBeginning(topicPartitionSet);

            logger.debug("Consumer sought to beginning, polling for records");
        }

        return resource.endOffsets(topicPartitionSet);
    }

    public static String formatCoordinator(Node coordinator){
        return String.format("%s (%s:%s)", coordinator.id(), coordinator.host(), coordinator.port());
    }
}
