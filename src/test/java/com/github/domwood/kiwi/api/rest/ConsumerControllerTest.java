package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.error.ApiError;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
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
import static com.github.domwood.kiwi.testutils.TestDataFactory.buildConsumerRequest;
import static com.github.domwood.kiwi.testutils.TestDataFactory.buildConsumerResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsumerControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaTaskProvider kafkaTaskProvider;

    @Autowired
    private ConsumerController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private BasicConsumeMessages basicConsumeMessages;

    private String url;

    @BeforeEach
    public void beforeEach(){
        this.url = "http://localhost:" + port + "/api/consume";
        when(kafkaTaskProvider.basicConsumeMessages(any(ConsumerRequest.class))).thenReturn(basicConsumeMessages);
    }

    @Test
    public void contexLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void testConsumerRequest() throws JsonProcessingException{
        when(basicConsumeMessages.execute())
                .thenReturn(CompletableFuture.completedFuture(buildConsumerResponse().build()));

        assertEquals(consumerResponseAsString(), testPostToUrl(this.restTemplate, this.url, consumerRequestAsString()));

        verify(basicConsumeMessages, times(1)).execute();
    }

    @Test
    public void testConsumeFailure() throws IOException, JSONException {

        String message = "Failed to consume from kafka";
        CompletableFuture<ConsumerResponse<String, String>> request = new CompletableFuture<>();
        request.completeExceptionally(new KafkaException(message));

        when(basicConsumeMessages.execute())
                .thenReturn(request);

        String response = testPostToUrl(this.restTemplate, this.url, consumerRequestAsString());
        ApiError error = objectMapper.readValue(response, ApiError.class);

        assertEquals(KafkaException.class.getName(), error.rootCause());
        assertEquals(message, error.message());

        verify(basicConsumeMessages, times(1)).execute();
    }

    private String consumerRequestAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(buildConsumerRequest().build());
    }

    private String consumerResponseAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(buildConsumerResponse().build());
    }

}
