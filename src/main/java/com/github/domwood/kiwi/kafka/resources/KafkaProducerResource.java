package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.kafka.exceptions.KafkaResourceClientCloseException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;

public class KafkaProducerResource<K, V> extends AbstractKafkaResource<KafkaProducer<K, V>> {

    public KafkaProducerResource(Properties props){
        super(props);
    }

    protected KafkaProducer<K, V> createClient(Properties props){
        return new KafkaProducer<>(props);
    }

    @Override
    protected void closeClient() throws KafkaResourceClientCloseException {
        try {
            this.getClient().close(20, TimeUnit.SECONDS);
        }
        catch (Exception e){
            throw new KafkaResourceClientCloseException("Failed to cleanly close client, due to "+e.getMessage(), e);
        }
    }

    public CompletableFuture<RecordMetadata> send(ProducerRecord<K, V> record){
        return toCompletable(this.getClient().send(record));
    }

}
