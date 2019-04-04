package com.github.domwood.kiwi.kafka.configs;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public abstract class KafkaConfig {

    protected String bootstrapServers;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    protected Properties createConfig(String localServers){
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
