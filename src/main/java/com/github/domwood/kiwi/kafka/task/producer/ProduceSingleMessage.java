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

public class ProduceSingleMessage extends AbstractKafkaTask<ProducerRequest, ProducerResponse, KafkaProducerResource<String, String>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProduceSingleMessage(KafkaProducerResource<String, String> resource, ProducerRequest input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<ProducerResponse> delegateExecute() {
        try {

            String topic = input.topic();
            String key = input.key();
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, null, key, input.payload().orElse(null), toKafkaHeaders(input.headers()));

            logger.info("Attempting to {} to {} with key {}", input.payload().isPresent() ? "Produce": "Tombstone", topic, key);

            return resource.send(record)
                    .thenApply(result -> onSuccess(result, resource));
        }
        catch (Exception e){
            logger.error("Failed to execute produce single message task", e);
            return failedFuture(e);
        }
    }

    private ProducerResponse onSuccess(RecordMetadata recordMetadata, KafkaProducerResource resource){
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
