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

    private String groupIdPrefix;
    private String groupIdSuffix;
    private String enableAutoCommit;
    private String autoOffsetReset;
    private String maxPollRecords;

    public String getGroupIdPrefix() {
        return groupIdPrefix;
    }

    public void setGroupIdPrefix(String groupIdPrefix) {
        this.groupIdPrefix = groupIdPrefix;
    }

    public String getGroupIdSuffix() {
        return groupIdSuffix;
    }

    public void setGroupIdSuffix(String groupIdSuffix) {
        this.groupIdSuffix = groupIdSuffix;
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

    public String getMaxPollRecords() {
        return maxPollRecords;
    }

    public void setMaxPollRecords(String maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }

    public Properties createStringConfig(Optional<String> bootstrapServers){
        Properties props = baseConfig(bootstrapServers);

        //TODO improve this mechanism
        if(groupIdPrefix != null) props.setProperty("groupIdPrefix", groupIdPrefix);
        if(groupIdSuffix != null) props.setProperty("groupIdSuffix", groupIdSuffix);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    private Properties baseConfig(Optional<String>  bootstrapServers){
        Properties properties = super.createConfig(bootstrapServers);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        return properties;
    }

}
