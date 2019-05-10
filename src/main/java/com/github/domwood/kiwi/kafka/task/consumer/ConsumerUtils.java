package com.github.domwood.kiwi.kafka.task.consumer;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MILLIS;

public class ConsumerUtils {
    private static Logger logger = LoggerFactory.getLogger(ConsumerUtils.class);

    static Map<TopicPartition, Long> subscribeAndSeek(KafkaConsumerResource<String, String> resource,
                                    ConsumerRequest input){
        resource.subscribe(input.topics());

        logger.debug("Consumer awaiting assignment for {} ...", input.topics());

        Set<TopicPartition> topicPartitionSet = resource.assignment();

        while(topicPartitionSet.isEmpty()){
            resource.poll(Duration.of(10, MILLIS));
            topicPartitionSet = resource.assignment();
        }

        logger.debug("Consumer attained assignment {} for {}. Seeking to beginning ...", topicPartitionSet, input.topics());

        resource.seekToBeginning(topicPartitionSet);
        Map<TopicPartition, Long> endOffsets = resource.endOffsets(topicPartitionSet);

        logger.debug("Consumer sought to beginning, polling for records");
        return endOffsets;
    }
}
