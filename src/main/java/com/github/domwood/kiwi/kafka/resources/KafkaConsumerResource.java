package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class KafkaConsumerResource<K, V> extends KafkaResource<KafkaConsumer<K, V>> {

    public KafkaConsumerResource(Properties config) {
        super(config);
    }

    @Override
    protected KafkaConsumer<K, V> createClient(Properties props) {
        return new KafkaConsumer<>(this.config);
    }

    @Override
    protected void closeClient() throws Exception {
        //TODO sort out concurrent modification issue
        this.client.close();
    }

    public void subscribe(List<String> topics){
        this.client.subscribe(topics);
    }

    public Set<TopicPartition> assignment(){
        return this.client.assignment();
    }

    public ConsumerRecords<K, V> poll(Duration timeout){
        return this.client.poll(timeout);
    }

    public void seekToBeginning(Set<TopicPartition> topicPartitions){
        this.client.seekToBeginning(topicPartitions);
    }

    public Map<TopicPartition, Long> endOffsets(Set<TopicPartition> topicPartitions){
        return this.client.endOffsets(topicPartitions);
    }

    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback){
        this.client.commitAsync(offsets, callback);
    }

}
