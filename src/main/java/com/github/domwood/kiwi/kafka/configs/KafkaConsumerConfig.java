package com.github.domwood.kiwi.kafka.configs;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConsumerConfig extends KafkaConfig{

    private String groupId;
    private String enableAutoCommit;
    private String autoOffsetReset;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getEnableAutoCommit() {
        return enableAutoCommit;
    }

    public void setEnableAutoCommit(String enableAutoCommit) {
        this.enableAutoCommit = enableAutoCommit;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }

    public Properties createStringConfig(Optional<String> bootstrapServers){
        Properties props = baseConfig(bootstrapServers);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    private Properties baseConfig(Optional<String>  bootstrapServers){
        Properties properties = super.createConfig(bootstrapServers);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        return properties;
    }

}
