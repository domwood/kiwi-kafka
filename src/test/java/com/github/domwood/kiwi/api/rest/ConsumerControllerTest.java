package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableConsumedMessage;
import com.github.domwood.kiwi.data.output.ImmutableConsumerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import org.apache.kafka.clients.consumer.CommitFailedException;
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

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.testutils.HttpTestUtils.testPostToUrl;
import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static java.util.Arrays.asList;
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
                .thenReturn(CompletableFuture.completedFuture(consumerResponse()));

        assertEquals(consumerResponseAsString(), testPostToUrl(this.restTemplate, this.url, consumerRequestAsString()));

        verify(basicConsumeMessages, times(1)).execute();
    }

    private String consumerRequestAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(buildConsumerRequest(testTopic));
    }

    private String consumerResponseAsString() throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(consumerResponse());
    }

    private ConsumerResponse<String, String> consumerResponse(){
        return ImmutableConsumerResponse.<String, String>builder()
                .messages(asList(ImmutableConsumedMessage.<String, String>builder()
                        .key(testKey)
                        .headers(testHeaders)
                        .message("Hello World")
                        .offset(10)
                        .partition(1)
                        .timestamp(0L)
                        .build()))
                .build();
    }

}
