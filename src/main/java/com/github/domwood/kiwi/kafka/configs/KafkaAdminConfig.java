package com.github.domwood.kiwi.kafka.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "kafka.admin")
public class KafkaAdminConfig extends KafkaConfig {

    public Properties createConfig(Optional<String> localServers){
        return super.createConfig(localServers);
    }
}
