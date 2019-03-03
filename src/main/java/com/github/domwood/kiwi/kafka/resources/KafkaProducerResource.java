package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaProducerResource<K, V> extends KafkaResource<KafkaProducer<K, V>>{

    public KafkaProducerResource(Properties props){
        super(props);
    }

    protected KafkaProducer<K, V> createClient(Properties props){
        return new KafkaProducer<>(props);
    }

    @Override
    protected void closeClient() throws Exception {
        this.client.close(20, TimeUnit.SECONDS);
    }

}
