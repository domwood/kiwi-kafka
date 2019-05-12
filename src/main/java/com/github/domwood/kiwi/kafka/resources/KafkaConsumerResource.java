package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.consumer.*;
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
        Properties properties =  new Properties();
        properties.putAll(props);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, Thread.currentThread().getName());
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, Thread.currentThread().getName());
        return new KafkaConsumer<>(properties);
    }

    @Override
    protected void closeClient() throws Exception {
        //TODO sort out concurrent modification issue
        this.getClient().close();
    }

    public void subscribe(List<String> topics){
        this.getClient().subscribe(topics);
    }

    public Set<TopicPartition> assignment(){
        return this.getClient().assignment();
    }

    public ConsumerRecords<K, V> poll(Duration timeout){
        return this.getClient().poll(timeout);
    }

    public void seekToBeginning(Set<TopicPartition> topicPartitions){
        this.getClient().seekToBeginning(topicPartitions);
    }

    public Map<TopicPartition, Long> endOffsets(Set<TopicPartition> topicPartitions){
        return this.getClient().endOffsets(topicPartitions);
    }

    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback){
        this.getClient().commitAsync(offsets, callback);
    }

}
