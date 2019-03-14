package com.github.domwood.kiwi.kafka.configs;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConsumerConfig extends KafkaConfig{

    public String groupId;
    public String enableAutCommit;
    public String autoOffsetReset;
    public String keyDeserializerClass;
    public String valueDeserilizerClass;


    public Properties createConfig(){
        Properties properties = super.createConfig();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId + Thread.currentThread().getName()); //Expects to be called on a kiwi task thread
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutCommit);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass);
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserilizerClass);
        return properties;
    }

}
