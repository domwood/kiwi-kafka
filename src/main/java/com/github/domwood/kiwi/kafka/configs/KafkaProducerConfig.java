package com.github.domwood.kiwi.kafka.configs;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "kafka.producer")
public class KafkaProducerConfig extends KafkaConfig {

    public Properties createStringConfig(Optional<String> localServers){
        Properties properties = super.createConfig(localServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }
}
