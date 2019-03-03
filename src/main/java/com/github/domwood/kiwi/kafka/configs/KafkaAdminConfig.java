package com.github.domwood.kiwi.kafka.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.admin")
public class KafkaAdminConfig extends KafkaConfig {

}
