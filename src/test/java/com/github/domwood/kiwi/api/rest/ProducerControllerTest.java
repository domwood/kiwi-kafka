package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.error.ApiError;
import com.github.domwood.kiwi.data.input.ProducerRequest;
import com.github.domwood.kiwi.data.output.ProducerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.producer.ProduceSingleMessage;
import org.apache.kafka.common.KafkaException;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.testutils.HttpTestUtils.testPostToUrl;
import static com.github.domwood.kiwi.testutils.TestDataFactory.buildProducerRequest;
import static com.github.domwood.kiwi.testutils.TestDataFactory.buildProducerResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProducerControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaTaskProvider kafkaTaskProvider;

    @Autowired
    private ProducerController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ProduceSingleMessage<String, String> produceSingleMessage;

    private String url;

    @BeforeEach
    public void beforeEach() {
        this.url = "http://localhost:" + port + "/api/produce";
        when(kafkaTaskProvider.<String, String>produceSingleMessage(any(ProducerRequest.class))).thenReturn(produceSingleMessage);
    }

    @Test
    public void contexLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void testProduceRequest() throws JsonProcessingException, JSONException {
        when(produceSingleMessage.execute())
                .thenReturn(CompletableFuture.completedFuture(buildProducerResponse().build()));

        assertEquals(producerResponseAsString(), testPostToUrl(this.restTemplate, this.url, producerRequestAsString()));

        verify(produceSingleMessage, times(1)).execute();
    }

    @Test
    public void testProduceFailed() throws IOException, JSONException {

        String message = "Failed to produce to kafka";
        CompletableFuture<ProducerResponse> request = new CompletableFuture<>();
        request.completeExceptionally(new KafkaException(message));

        when(produceSingleMessage.execute())
                .thenReturn(request);

        String response = testPostToUrl(this.restTemplate, this.url, producerRequestAsString());
        ApiError error = objectMapper.readValue(response, ApiError.class);

        assertEquals(KafkaException.class.getName(), error.rootCause());
        assertEquals(message, error.message());

        verify(produceSingleMessage, times(1)).execute();
    }

    private String producerRequestAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(buildProducerRequest().build());
    }

    private String producerResponseAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(buildProducerResponse().build());
    }

}
