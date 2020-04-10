package com.github.domwood.kiwi.kafka.configs;

import com.github.domwood.kiwi.utilities.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@ConfigurationProperties("kafka")
public class KafkaConfigManager {

    private final String defaultCluster = "default";
    private final String clientConfigKey = "client";
    private final String consumerConfigKey = "consumer";
    private final String producerConfigKey = "producer";
    private final String adminConfigKey = "admin";

    private final Map<String, Map<String, Map<String, String>>> clusters = new HashMap<>();
    private final Map<String, Map<String, Map<String, String>>> mutableClusterCopy = new HashMap<>();

    @PostConstruct
    public void setMutableClusterCopy() {
        this.mutableClusterCopy.putAll(clusters);
    }

    public Map<String, Map<String, Map<String, String>>> getClusters() {
        return clusters;
    }

    public Map<String, Map<String, Map<String, String>>> getClusterConfiguration() {
        return mutableClusterCopy;
    }

    public Properties generateConsumerConfig(Optional<String> clusterName) {
        return generateConfig(clusterName, consumerConfigKey);
    }

    public Properties generateProducerConfig(Optional<String> clusterName) {
        return generateConfig(clusterName, producerConfigKey);
    }

    public Properties generateAdminConfig(Optional<String> clusterName) {
        return generateConfig(clusterName, adminConfigKey);
    }

    public void updateClusterConfiguration(String clusterName, Map<String, Map<String, String>> clusterConfig) {
        this.mutableClusterCopy.put(clusterName, clusterConfig);
    }

    private Properties generateConfig(Optional<String> clusterName, String targetClientType) {
        Map<String, Map<String, String>> cluster = mutableClusterCopy.getOrDefault(clusterName.orElse(defaultCluster), Collections.emptyMap());
        Map<String, String> clientConfig = new HashMap<>(cluster.getOrDefault(clientConfigKey, Collections.emptyMap()));
        Map<String, String> consumerConfig = new HashMap<>(cluster.getOrDefault(targetClientType, Collections.emptyMap()));
        Properties props = new Properties();
        clientConfig.forEach((key, value) -> props.setProperty(StringUtils.convertCamelToKafkaConfigFormat(key), value));
        consumerConfig.forEach((key, value) -> props.setProperty(StringUtils.convertCamelToKafkaConfigFormat(key), value));
        return props;
    }
}


