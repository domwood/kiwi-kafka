package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

public class KafkaConsumerResource<K, V> extends AbstractKafkaResource<KafkaConsumer<K, V>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Properties properties;
    private final KafkaDataTypeHandler<K> keyConverter;
    private final KafkaDataTypeHandler<V> valueConverter;

    public KafkaConsumerResource(Properties config, KafkaDataTypeHandler<K> keyConverter, KafkaDataTypeHandler<V> valueConverter) {
        super(config);
        this.properties = new Properties();
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    @Override
    protected KafkaConsumer<K, V> createClient(ImmutableMap<Object, Object> props) {
        String groupIdPrefix = props.getOrDefault("groupIdPrefix", "").toString();
        String groupIdSuffix = props.getOrDefault("groupIdSuffix", "").toString();
        String groupId = String.format("%s%s%s", groupIdPrefix, Thread.currentThread().getName(), groupIdSuffix);

        this.properties = new Properties();
        properties.putAll(props);
        properties.remove("groupIdPrefix");
        properties.remove("groupIdSuffix");

        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyConverter.getKafkaDeserializer());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueConverter.getKafkaDeserializer());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, groupId);
        return new KafkaConsumer<>(properties);
    }

    @Override
    protected void closeClient() throws KafkaResourceClientCloseException {
        //TODO sort out concurrent modification issue
        logger.info("Kafka consumer client closing...");
        try {
            this.getClient().unsubscribe();
        } catch (Exception e) {
            logger.warn("Failed to cleanly unsubscribe");
        }
        try {
            this.getClient().close();
            logger.info("Kafka consumer closed for groupId: %s" + this.getGroupId());
        } catch (Exception e) {
            throw new KafkaResourceClientCloseException("Failed to cleanly close Resource, due to " + e.getMessage(), e);
        }
    }

    public boolean isCommittingConsumer() {
        return Optional.ofNullable(this.properties.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG))
                .orElse("true")
                .equals("true");
    }

    public void subscribe(List<String> topics) {
        this.getClient().subscribe(topics);
    }

    public Set<TopicPartition> assignment() {
        return this.getClient().assignment();
    }

    public ConsumerRecords<K, V> poll(Duration timeout) {
        return this.getClient().poll(timeout);
    }

    public void seekToBeginning(Set<TopicPartition> topicPartitions) {
        this.getClient().seekToBeginning(topicPartitions);
    }

    public void seek(Map<TopicPartition, Long> topicPartitions) {
        topicPartitions.forEach((k, v) -> this.getClient().seek(k, v));
    }

    public Map<TopicPartition, Long> endOffsets(Set<TopicPartition> topicPartitions) {
        return this.getClient().endOffsets(topicPartitions);
    }

    public Map<TopicPartition, Long> currentPosition(Set<TopicPartition> topicPartitions) {
        return topicPartitions.stream()
                .map(tp -> Pair.of(tp, getClient().position(tp)))
                .collect(toMap(Pair::getLeft, Pair::getRight));
    }

    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
        this.getClient().commitAsync(offsets, callback);
    }

    public void unsubscribe() {
        try {
            this.getClient().unsubscribe();
            logger.info("Unsubscribing client from topics");
        } catch (Exception e) {
            logger.warn("Failed to unsubscribe client", e);
        }
    }

    public String getGroupId() {
        return this.properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
    }

    public String convertKafkaKey(K key) {
        return this.keyConverter.convert(key);
    }

    public String convertKafkaValue(V value) {
        return this.valueConverter.convert(value);
    }
}
