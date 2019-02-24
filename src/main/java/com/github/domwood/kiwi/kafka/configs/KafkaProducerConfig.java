package com.github.domwood.kiwi.kafka.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "kafka.producer")
public class KafkaProducerConfig {

    public Properties createConfig(){
        return new Properties();
    }
}
