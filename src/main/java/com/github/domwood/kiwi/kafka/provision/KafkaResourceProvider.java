package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.kafka.configs.KafkaAdminConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaConsumerConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaProducerConfig;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
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

    public <K, V> KafkaConsumerResource<K, V> kafkaConsumerResource(String bootStrapServers){
        return new KafkaConsumerResource<>(consumerConfig.createConfig(bootStrapServers));
    }

    public <K, V> KafkaProducerResource<K, V> kafkaProducerResource(String bootStrapServers){
        return new KafkaProducerResource<>(producerConfig.createConfig(bootStrapServers));
    }

    public KafkaAdminResource kafkaAdminResource(String bootStrapServers){
        Properties props = adminConfig.createConfig(bootStrapServers);
        return new KafkaAdminResource(props);
    }
}
