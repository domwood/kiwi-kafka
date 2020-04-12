package com.github.domwood.kiwi.kafka.configs;

import com.google.common.collect.ImmutableSet;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@ConfigurationProperties("kafka")
public class KafkaConfigManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String defaultCluster = "default";
    private final String clientConfigKey = "client";
    private final String consumerConfigKey = "consumer";
    private final String producerConfigKey = "producer";
    private final String adminConfigKey = "admin";

    private final Map<String, Map<String, String>> base = new HashMap<>();

    private final Map<String, Map<String, Map<String, String>>> clusters = new HashMap<>();

    private final Map<String, Map<String, Map<String, String>>> mutableClusterCopy = new HashMap<>();

    //Backwards compatability from =< 0.5.0 version of kiwi
    @Deprecated
    private String bootstrapServers;

    private final Map<String, String> configMaps = new HashMap<>();

    public KafkaConfigManager() {
        addAllConfigs(ConsumerConfig.configNames());
        addAllConfigs(ProducerConfig.configNames());
        addAllConfigs(AdminClientConfig.configNames());
    }

    private void addAllConfigs(Set<String> configNames) {
        configNames.forEach(name -> configMaps.put(name.replaceAll("\\.", "").toLowerCase(), name));
    }

    @PostConstruct
    public void setMutableClusterCopy() {
        this.mutableClusterCopy.putAll(clusters);
        if(this.mutableClusterCopy.isEmpty()){
            this.mutableClusterCopy.put(defaultCluster, new HashMap<>());
        }
        if(bootstrapServers != null && !bootstrapServers.isEmpty()){
            logger.warn("Using a deprecated configuration parameter use 'kafka.base.client.bootstrapServers' or configure a cluster configuration");
            Map<String, String> baseClient = this.base.getOrDefault("client", new HashMap<>());
            baseClient.put("bootstrapServers", bootstrapServers);
            this.base.put("client", baseClient);
        }
    }

    public Map<String, Map<String, Map<String, String>>> getClusters() {
        return clusters;
    }

    public Map<String, Map<String, String>> getBase() {
        return base;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public Set<String> getClusterList() {
        Set<String> clusterNames = mutableClusterCopy.keySet();
        return clusterNames.isEmpty() ? ImmutableSet.of(defaultCluster) : clusterNames;
    }

    public Map<String, Map<String, Map<String, String>>> getActiveClusterConfiguration() {
        //TODO merge with base config values before returning
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

    public void resetConfig(){
        this.mutableClusterCopy.clear();
        this.mutableClusterCopy.putAll(clusters);
    }

    private Properties generateConfig(Optional<String> clusterName, String targetClientType) {
        Properties props = new Properties();
        String targetCluster = clusterName.orElse(defaultCluster);
        Map<String, Map<String, String>> cluster = mutableClusterCopy.getOrDefault(targetCluster, Collections.emptyMap());

        Map<String, String> baseClientConfig = base.getOrDefault(clientConfigKey, Collections.emptyMap());
        baseClientConfig.forEach((key, value) -> props.setProperty(findKafkaPropertyKey(key), value));

        Map<String, String> baseClientTypeConfig = base.getOrDefault(targetClientType, Collections.emptyMap());
        baseClientTypeConfig.forEach((key, value) -> props.setProperty(findKafkaPropertyKey(key), value));

        Map<String, String> clientConfig = new HashMap<>(cluster.getOrDefault(clientConfigKey, Collections.emptyMap()));
        clientConfig.forEach((key, value) -> props.setProperty(findKafkaPropertyKey(key), value));

        Map<String, String> clientTypeConfig = new HashMap<>(cluster.getOrDefault(targetClientType, Collections.emptyMap()));
        clientTypeConfig.forEach((key, value) -> props.setProperty(findKafkaPropertyKey(key), value));

        overrideValues(props);
        logger.info("Configuration for cluster [{}] provided for resource creation", targetCluster);
        return props;
    }

    private String findKafkaPropertyKey(String springConfig) {
        return this.configMaps.getOrDefault(springConfig.toLowerCase(), springConfig);
    }

    //Annoying issue for ssl.endpoint.identification.algorithm
    private void overrideValues(Properties props) {
        if ("none".equals(props.getProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG))) {
            props.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        }
    }
}


