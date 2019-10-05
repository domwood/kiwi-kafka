package com.github.domwood.kiwi.kafka.task.producer;

import com.github.domwood.kiwi.data.output.ProducerResponse;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProduceSingleMessageTest {

    @Mock
    KafkaProducerResource<String, String> producerResource;

    @DisplayName("Successfully produce a message to the kafka resource")
    @Test
    public void produceMessageTest() throws InterruptedException, ExecutionException, TimeoutException {

        when(producerResource.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(recordMetadata()));

        ProduceSingleMessage produceSingleMessage = new ProduceSingleMessage(producerResource, buildProducerRequest().build());

        ProducerResponse observed = produceSingleMessage.execute().get(1, TimeUnit.SECONDS);

        ProducerResponse expected = buildProducerResponse().build();
        assertEquals(expected, observed);
    }

    @DisplayName("Produce message task handles an exception scenario")
    @Test
    public void failureProduceTest(){
        when(producerResource.send(any(ProducerRecord.class))).thenThrow(new KafkaException("Producer Error of some sort"));

        ProduceSingleMessage produceSingleMessage = new ProduceSingleMessage(producerResource, buildProducerRequest().build());

        CompletableFuture<ProducerResponse> future = produceSingleMessage.execute();

        await().atMost(1, TimeUnit.SECONDS)
                .until(future::isCompletedExceptionally);

        assertThrows(ExecutionException.class, future::get);

    }

    private RecordMetadata recordMetadata(){
        return new RecordMetadata(new TopicPartition(testTopic,testPartition), 0, testOffset, testTimestamp, 0L, 0, 0);
    }
}
