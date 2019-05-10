package com.github.domwood.kiwi.kafka.configs;

import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Optional;
import java.util.Properties;

public abstract class KafkaConfig {

    protected String bootstrapServers;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    protected Properties createConfig(Optional<String> localServers){
        Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, localServers.orElse(bootstrapServers));
        return properties;
    }

}
