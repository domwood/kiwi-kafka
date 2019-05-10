package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.api.rest.ProducerController;
import com.github.domwood.kiwi.data.input.ImmutableProducerRequest;
import com.github.domwood.kiwi.data.input.ProducerRequest;
import com.github.domwood.kiwi.data.output.ImmutableProducerResponse;
import com.github.domwood.kiwi.data.output.ProducerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.producer.ProduceSingleMessage;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProducerControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaResourceProvider kafkaResourceProvider;

    @MockBean
    private KafkaTaskProvider kafkaTaskProvider;

    @Autowired
    private ProducerController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ProduceSingleMessage produceSingleMessage;

    private String url;

    @BeforeEach
    public void beforeEach(){
        this.url = "http://localhost:" + port + "/api/produce";
        when(kafkaTaskProvider.produceSingleMessage()).thenReturn(produceSingleMessage);
    }

    @Test
    public void contexLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void testProduceRequest() throws JsonProcessingException, JSONException {
        when(produceSingleMessage.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(producerResponse()));

        assertEquals(producerResponseAsString(), testPost(producerRequestAsString()));

        verify(produceSingleMessage, times(1)).execute(eq(null), eq(produceRequest()));
    }

    private String testPost(String data){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        return this.restTemplate.exchange(this.url,
                HttpMethod.POST,
                new HttpEntity<>(data, headers),
                String.class).getBody();
    }

    public String producerRequestAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(produceRequest());
    }

    public String producerResponseAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(producerResponse());
    }

    public ProducerRequest produceRequest() {
        return ImmutableProducerRequest.builder()
                .key("afda94e4-52d2-11e9-bdfd-af0113f0a512")
                .payload("Hello World")
                .putHeader("Test", "Header")
                .topic("TestTopic1")
                .build();
    }

    public ProducerResponse producerResponse() {
        return ImmutableProducerResponse.builder()
                .offset(10L)
                .partition(10)
                .topic("TestTopic1")
                .build();
    }

}
