package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

public class KafkaConsumerResource<K, V> extends AbstractKafkaResource<KafkaConsumer<K, V>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
            logger.info("Kafka consumer closed");
        }
        catch (Exception e){
            throw new KafkaResourceClientCloseException("Failed to cleanly close WebSocketService, due to "+e.getMessage(), e);
        }
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

}
