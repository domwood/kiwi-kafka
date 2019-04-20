package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

public class KafkaConsumerResource<K, V>  extends KafkaResource<KafkaConsumer<K, V>>{

    private KafkaConsumer<K,V> kafkaConsumer;

    public KafkaConsumerResource(Properties config){
        super(config);
    }

    public void finish() {
        this.kafkaConsumer.unsubscribe();
    }

    public void discard() {
        this.kafkaConsumer.close();
        this.kafkaConsumer = null;
    }

    @Override
    protected KafkaConsumer<K, V> createClient(Properties props) {
        if(kafkaConsumer == null){
            this.kafkaConsumer = new KafkaConsumer<>(this.config);
        }
        return this.kafkaConsumer;
    }

    @Override
    protected void closeClient() throws Exception {
        this.kafkaConsumer.close();
        this.kafkaConsumer = null;
    }

}
