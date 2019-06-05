package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.kafka.configs.KafkaAdminConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaConsumerConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaProducerConfig;
import com.github.domwood.kiwi.kafka.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

//TODO we can't really close these resources externally
// they need to be closed by the thread which created the
// underlying client, else we throw concurrent modification exes
// So many of these cleanup methods aren't suitable and need rethought
@Component
public class KafkaResourceProvider {

    private final Logger logger = LoggerFactory.getLogger(KafkaResourceProvider.class);
    private final KafkaAdminConfig adminConfig;
    private final KafkaConsumerConfig consumerConfig;
    private final KafkaProducerConfig producerConfig;

    @Autowired
    public KafkaResourceProvider(KafkaAdminConfig adminConfig,
                                 KafkaConsumerConfig consumerConfig,
                                 KafkaProducerConfig producerConfig){
        this.adminConfig = adminConfig;
        this.consumerConfig = consumerConfig;
        this.producerConfig = producerConfig;
    }

    public KafkaConsumerResource<String, String> kafkaStringConsumerResource(Optional<String> bootStrapServers){
        KafkaConsumerResource consumerResource = new KafkaConsumerResource<>(consumerConfig.createStringConfig(bootStrapServers));
        return consumerResource;
    }

    public KafkaProducerResource<String, String> kafkaStringProducerResource(Optional<String> bootStrapServers){
        KafkaProducerResource producerResource = new KafkaProducerResource<>(producerConfig.createStringConfig(bootStrapServers));
        return producerResource;
    }

    public KafkaAdminResource kafkaAdminResource(Optional<String> bootStrapServers){
        Properties props = adminConfig.createConfig(bootStrapServers);
        KafkaAdminResource adminResource = new KafkaAdminResource(props);
        return adminResource;
    }

    public KafkaTopicConfigResource kafkaTopicConfigResource(){
        return new KafkaTopicConfigResource(null);
    }

    public KafkaResourcePair<KafkaAdminResource, KafkaConsumerResource<String,String>> kafkaAdminAndConsumer(Optional<String> bootStrapServers){
        KafkaAdminResource admin = this.kafkaAdminResource(bootStrapServers);
        KafkaConsumerResource<String, String> consumer = kafkaStringConsumerResource(bootStrapServers);
        return new KafkaResourcePair<>(admin, consumer);
    }


}
