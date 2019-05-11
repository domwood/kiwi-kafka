package com.github.domwood.kiwi.kafka.task.consumer;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import com.github.domwood.kiwi.data.output.ImmutableConsumedMessage;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.fromKafkaHeaders;
import static java.time.temporal.ChronoUnit.MILLIS;

public interface ConsumerUtils {
    Logger logger = LoggerFactory.getLogger(ConsumerUtils.class);

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

    static void logCommit(Map<TopicPartition, OffsetAndMetadata> offsetData, Exception exception){
        if(exception != null){
            logger.error("Failed to commit offset ", exception);
        }
        else{
            logger.debug("Commit offset data {}", offsetData);
        }
    }

    static ConsumedMessage<String, String> asConsumedRecord(ConsumerRecord<String, String> record){
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
