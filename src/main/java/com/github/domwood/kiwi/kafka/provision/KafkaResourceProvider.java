package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.kafka.configs.KafkaAdminConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaConsumerConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaProducerConfig;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaTopicConfigResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;


@Component
public class KafkaResourceProvider {

    private KafkaAdminConfig adminConfig;
    private KafkaConsumerConfig consumerConfig;
    private KafkaProducerConfig producerConfig;

    @Autowired
    public KafkaResourceProvider(KafkaAdminConfig adminConfig,
                                 KafkaConsumerConfig consumerConfig,
                                 KafkaProducerConfig producerConfig){
        this.adminConfig = adminConfig;
        this.consumerConfig = consumerConfig;
        this.producerConfig = producerConfig;
    }

    public KafkaConsumerResource<String, String> kafkaStringConsumerResource(String bootStrapServers){
        return new KafkaConsumerResource<>(consumerConfig.createStringConfig(bootStrapServers));
    }

    public KafkaProducerResource<String, String> kafkaStringProducerResource(String bootStrapServers){
        return new KafkaProducerResource<>(producerConfig.createStringConfig(bootStrapServers));
    }

    public KafkaAdminResource kafkaAdminResource(String bootStrapServers){
        Properties props = adminConfig.createConfig(bootStrapServers);
        return new KafkaAdminResource(props);
    }

    public KafkaTopicConfigResource kafkaTopicConfigResource(){
        return new KafkaTopicConfigResource(null);
    }
}
