package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.data.input.KafkaDataType;
import com.github.domwood.kiwi.kafka.configs.KafkaConfigManager;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaDataTypeHandler;
import com.github.domwood.kiwi.kafka.resources.KafkaDataTypeHandlerProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaResourcePair;
import com.github.domwood.kiwi.kafka.resources.KafkaTopicConfigResource;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

@Component
public class KafkaResourceProvider {

    private final KafkaConfigManager configManager;

    public KafkaResourceProvider(KafkaConfigManager configManager) {
        this.configManager = configManager;
    }

    public <K, V> KafkaConsumerResource<K, V> kafkaConsumerResource(Optional<String> clusterName,
                                                                    KafkaDataTypeHandler<K> keyHandler,
                                                                    KafkaDataTypeHandler<V> valueHandler) {
        return new KafkaConsumerResource<>(configManager.generateConsumerConfig(clusterName), keyHandler, valueHandler);
    }

    public <K, V> KafkaProducerResource<K, V> kafkaProducerResource(Optional<String> clusterName,
                                                                    KafkaDataTypeHandler<K> keyHandler,
                                                                    KafkaDataTypeHandler<V> valueHandler) {
        return new KafkaProducerResource<>(configManager.generateProducerConfig(clusterName), keyHandler, valueHandler);
    }

    public KafkaAdminResource kafkaAdminResource(Optional<String> clusterName) {
        return new KafkaAdminResource(configManager.generateAdminConfig(clusterName));
    }

    public KafkaTopicConfigResource kafkaTopicConfigResource() {
        return new KafkaTopicConfigResource(new Properties());
    }

    public KafkaResourcePair<KafkaAdminResource, KafkaConsumerResource<String, String>> kafkaAdminAndConsumer(Optional<String> clusterName) {
        KafkaAdminResource admin = this.kafkaAdminResource(clusterName);
        KafkaDataTypeHandler<String> dataTypeHandler = (KafkaDataTypeHandler<String>) KafkaDataTypeHandlerProvider.getTypeHandler(KafkaDataType.STRING);
        KafkaConsumerResource<String, String> consumer = kafkaConsumerResource(clusterName, dataTypeHandler, dataTypeHandler);
        return new KafkaResourcePair<>(admin, consumer);
    }

}
