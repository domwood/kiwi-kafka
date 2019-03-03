package com.github.domwood.kiwi.kafka.task.producer;

import com.github.domwood.kiwi.api.input.ProducerRequest;
import com.github.domwood.kiwi.api.output.ImmutableProducerResponse;
import com.github.domwood.kiwi.api.output.ProducerResponse;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.toKafkaHeaders;
import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;

public class ProduceSingleMessage implements KafkaTask<ProducerRequest, ProducerResponse, KafkaProducerResource<String, String>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CompletableFuture<ProducerResponse> execute(KafkaProducerResource<String, String> resource, ProducerRequest input) {
        try {
            KafkaProducer<String, String> producer = resource.provisionResource();

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(input.topic(), null, input.key(), input.payload().orElse(null), toKafkaHeaders(input.headers()));


            if(input.payload().isPresent()){
                logger.info("Attempting to produce to {} with key {}", input.topic(), input.key());
            }
            else{
                logger.info("Attempting to tombstone on {} for key {}", input.topic(), input.key());
            }

            return toCompletable(producer.send(record))
                    .thenApply(this::onSuccess);
        }
        catch (Exception e){
            logger.error("Failed to execute produce single message task", e);
            resource.discard();
            return failedFuture(e);
        }
    }

    private ProducerResponse onSuccess(RecordMetadata recordMetadata){
        String topic = recordMetadata.topic();
        int partition = recordMetadata.partition();
        long offset = recordMetadata.offset();

        logger.info("Produced successfully to {} on partition {} at offset {}", topic, partition, offset);

        return ImmutableProducerResponse.builder()
                .topic(topic)
                .offset(offset)
                .partition(partition)
                .build();
    }

}
