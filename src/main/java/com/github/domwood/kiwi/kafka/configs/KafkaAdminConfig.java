package com.github.domwood.kiwi.kafka.configs;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "kafka.admin")
public class KafkaAdminConfig {

    public String bootstrapServers;

    public Properties createConfig(){
        return createConfig(null);
    }

    public Properties createConfig(String localServers){
        Properties properties = new Properties();
        if(localServers != null){
            properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, localServers);
        }
        else{
            properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        }
        return properties;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
}
