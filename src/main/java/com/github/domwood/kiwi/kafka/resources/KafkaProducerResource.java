package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;

public class KafkaProducerResource<K, V> extends AbstractKafkaResource<KafkaProducer<K, V>> {

    private final KafkaDataTypeHandler<K> keyConverter;
    private final KafkaDataTypeHandler<V> valueConverter;

    public KafkaProducerResource(Properties props, KafkaDataTypeHandler<K> keyConverter, KafkaDataTypeHandler<V> valueConverter) {
        super(props);
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    protected KafkaProducer<K, V> createClient(ImmutableMap<Object, Object> props) {
        Properties properties = new Properties();
        properties.putAll(props);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keyConverter.getKafkaSerializer());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueConverter.getKafkaSerializer());
        return new KafkaProducer<>(properties);
    }

    @Override
    protected void closeClient() throws KafkaResourceClientCloseException {
        try {
            this.getClient().close(Duration.ofSeconds(20));
        } catch (Exception e) {
            throw new KafkaResourceClientCloseException("Failed to cleanly close WebSocketService, due to " + e.getMessage(), e);
        }
    }

    public CompletableFuture<RecordMetadata> send(ProducerRecord<K, V> record) {
        return toCompletable(this.getClient().send(record));
    }

    public K convertKafkaKey(String key) {
        return this.keyConverter.convert(key);
    }

    public V convertKafkaValue(String value) {
        return this.valueConverter.convert(value);
    }
}
