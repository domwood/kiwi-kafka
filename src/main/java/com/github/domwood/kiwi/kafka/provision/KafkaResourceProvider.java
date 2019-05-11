package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.kafka.configs.KafkaAdminConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaConsumerConfig;
import com.github.domwood.kiwi.kafka.configs.KafkaProducerConfig;
import com.github.domwood.kiwi.kafka.resources.*;
import com.github.domwood.kiwi.kafka.utils.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    private final ConcurrentLinkedQueue<KafkaResource<?>> resources;

    @Autowired
    public KafkaResourceProvider(KafkaAdminConfig adminConfig,
                                 KafkaConsumerConfig consumerConfig,
                                 KafkaProducerConfig producerConfig){
        this.resources = new ConcurrentLinkedQueue<>();
        this.adminConfig = adminConfig;
        this.consumerConfig = consumerConfig;
        this.producerConfig = producerConfig;
    }

    @PreDestroy
    public void beforeShutdown(){
        logger.info("Discarding all kafka resources, due to context shutdown");
        resources.forEach(KafkaResource::discard);
        resources.clear();
    }

    public KafkaConsumerResource<String, String> kafkaStringConsumerResource(Optional<String> bootStrapServers){
        KafkaConsumerResource consumerResource = new KafkaConsumerResource<>(consumerConfig.createStringConfig(bootStrapServers));
        resources.add(consumerResource);
        return consumerResource;
    }

    public KafkaProducerResource<String, String> kafkaStringProducerResource(Optional<String> bootStrapServers){
        KafkaProducerResource producerResource = new KafkaProducerResource<>(producerConfig.createStringConfig(bootStrapServers));
        resources.add(producerResource);
        return producerResource;
    }

    public KafkaAdminResource kafkaAdminResource(Optional<String> bootStrapServers){
        Properties props = adminConfig.createConfig(bootStrapServers);
        KafkaAdminResource adminResource = new KafkaAdminResource(props);
        resources.add(adminResource);
        return adminResource;
    }

    public KafkaTopicConfigResource kafkaTopicConfigResource(){
        KafkaTopicConfigResource topicConfigResource = new KafkaTopicConfigResource(null);
        resources.add(topicConfigResource);
        return new KafkaTopicConfigResource(null);
    }

    @Scheduled(fixedRate = 1000 * 60 * 5, fixedDelay = 1000 * 60)
    public void cleanUpStale(){
        logger.info("Discarding resources staler than 5mins");

        long stalestResourceAllowed = TimeProvider.getTime() - (1000 * 60 * 5);
        resources.forEach(res -> {
            if(res.getLastKeepAlive() < stalestResourceAllowed)res.discard();
        });
    }

    @Scheduled(fixedRate = 1000 * 60, fixedDelay = 1000 * 30)
    public void cleanUpDiscarded(){
        logger.info("Removing discarded resources");
        int count = resources.size();
        resources.removeIf(KafkaResource::isDiscarded);
        int update = resources.size();
        if(count - update > 0){
            logger.info("Removed {} discarded resources", count-update);
        }
    }

}
