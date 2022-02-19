package com.github.domwood.kiwi.kafka.task.producer;

import com.github.domwood.kiwi.data.input.ProducerRequest;
import com.github.domwood.kiwi.data.output.ImmutableProducerResponse;
import com.github.domwood.kiwi.data.output.ProducerResponse;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.toKafkaHeaders;
import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;

public class ProduceSingleMessage<K, V> extends AbstractKafkaTask<ProducerRequest, ProducerResponse, KafkaProducerResource<K, V>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProduceSingleMessage(KafkaProducerResource<K, V> resource, ProducerRequest input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<ProducerResponse> delegateExecute() {
        try {

            String topic = input.topic();
            K key = resource.convertKafkaKey(input.key());
            V recordValue = input.payload().map(resource::convertKafkaValue).orElse(null);
            ProducerRecord<K, V> producerRecord =
                    new ProducerRecord<>(topic, null, key, recordValue, toKafkaHeaders(input.headers()));

            logger.info("Attempting to {} to {} with key {}", input.payload().isPresent() ? "Produce" : "Tombstone", topic, key);

            return resource.send(producerRecord)
                    .thenApply(result -> onSuccess(result, resource));
        } catch (Exception e) {
            logger.error("Failed to execute produce single message task", e);
            return failedFuture(e);
        }
    }

    private ProducerResponse onSuccess(final RecordMetadata recordMetadata, final KafkaProducerResource<K, V> resource) {
        String topic = recordMetadata.topic();
        int partition = recordMetadata.partition();
        long offset = recordMetadata.offset();

        logger.info("Produced successfully to {} on partition {} at offset {}", topic, partition, offset);

        resource.discard();

        return ImmutableProducerResponse.builder()
                .topic(topic)
                .offset(offset)
                .partition(partition)
                .build();
    }

}
