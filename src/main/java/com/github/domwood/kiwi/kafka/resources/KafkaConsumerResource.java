package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class KafkaConsumerResource<K, V> extends AbstractKafkaResource<KafkaConsumer<K, V>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Properties properties;

    public KafkaConsumerResource(Properties config) {
        super(config);
    }

    @Override
    protected KafkaConsumer<K, V> createClient(Properties props) {
        //TODO improve passing these values
        String groupIdPrefix = props.getProperty("groupIdPrefix", "");
        String groupIdSuffix = props.getProperty("groupIdSuffix", "");
        String groupId = String.format("%s%s%s", groupIdPrefix, Thread.currentThread().getName(), groupIdSuffix);
        props.remove("groupIdPrefix");
        props.remove("groupIdSuffix");

        this.properties = new Properties();
        properties.putAll(props);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, groupId);
        return new KafkaConsumer<>(properties);
    }

    @Override
    protected void closeClient() throws KafkaResourceClientCloseException {
        //TODO sort out concurrent modification issue
        logger.info("Kafka consumer client closing...");
        try{
            this.getClient().unsubscribe();
        }
        catch (Exception e){
            logger.warn("Failed to cleanly unsubscribe");
        }
        try{
            this.getClient().close();
            logger.info("Kafka consumer closed for groupId: " + this.getGroupId());
        }
        catch (Exception e){
            throw new KafkaResourceClientCloseException("Failed to cleanly close WebSocketService, due to "+e.getMessage(), e);
        }
    }

    public boolean isCommittingConsumer(){
        return Optional.ofNullable(this.properties.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG))
                .orElse("true")
                .equals("true");
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

    public void seek(Map<TopicPartition, Long> topicPartitions){
        topicPartitions.forEach((k, v) -> this.getClient().seek(k, v));
    }

    public Map<TopicPartition, Long> endOffsets(Set<TopicPartition> topicPartitions){
        return this.getClient().endOffsets(topicPartitions);
    }

    public Map<TopicPartition, Long> currentPosition(Set<TopicPartition> topicPartitions){
        return topicPartitions.stream()
                .map(tp -> Pair.of(tp, getClient().position(tp)))
                .collect(toMap(Pair::getLeft, Pair::getRight));
    }

    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback){
        this.getClient().commitAsync(offsets, callback);
    }

    public void unsubscribe(){
        try{
            this.getClient().unsubscribe();
            logger.info("Unsubscribing client from topics");
        }
        catch (Exception e){
            logger.warn("Failed to unsubscribe client", e);
        }

    }

    public String getGroupId(){
        return this.properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
    }
}
