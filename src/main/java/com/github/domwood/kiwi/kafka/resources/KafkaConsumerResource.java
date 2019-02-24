package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public class KafkaConsumerResource<K, V> {

    private final Properties consumerConfig;

    private KafkaConsumer<K,V> kafkaConsumer;

    public KafkaConsumerResource(Properties config){
        this.consumerConfig = config;
    }

    public KafkaConsumer<K, V> provisionResource() {
        if(kafkaConsumer == null){
            this.kafkaConsumer = new KafkaConsumer<>(consumerConfig);
        }
        return this.kafkaConsumer;
    }

    public void finish() {
        this.kafkaConsumer.unsubscribe();
    }

    public void discard() {
        this.kafkaConsumer.close();
        this.kafkaConsumer = null;
    }

}
