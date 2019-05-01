package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MILLIS;

public class KafkaTaskUtils {
    private KafkaTaskUtils(){}

    public static <K, V> Set<TopicPartition> assignment(KafkaConsumerResource<K,V> consumerResource){
        Set<TopicPartition> topicPartitions;
        int maxCount = 100;
        while((topicPartitions = consumerResource.assignment()).isEmpty() && maxCount >0){
            consumerResource.poll(Duration.of(10, MILLIS));
            maxCount--;
        }
        return topicPartitions;
    }
}
