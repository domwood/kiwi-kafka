package com.github.domwood.kiwi.kafka.configs;

import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Properties;

public abstract class KafkaConfig {

    protected String bootstrapServers;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Properties createConfig(){
        return createConfig(null);
    }

    public Properties createConfig(String localServers){
        Properties properties = new Properties();
        if(localServers != null){
            properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, localServers);
        }
        else{
            properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        }
        return properties;
    }
}
