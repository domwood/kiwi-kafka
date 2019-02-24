package com.github.domwood.kiwi.kafka.resources;

import java.util.Properties;

public class KafkaProducerResource<K, V> {

    private final Properties producerConfig;

    public KafkaProducerResource(Properties props){
        this.producerConfig= props;
    }
}
