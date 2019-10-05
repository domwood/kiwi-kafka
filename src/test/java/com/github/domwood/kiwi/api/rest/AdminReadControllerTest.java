package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.error.ApiError;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableTopicList;
import com.github.domwood.kiwi.data.output.TopicList;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.admin.ConsumerGroupDetailsWithOffset;
import com.github.domwood.kiwi.kafka.task.admin.ListTopics;
import org.apache.kafka.common.KafkaException;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.testutils.HttpTestUtils.testPostToUrl;
import static com.github.domwood.kiwi.testutils.TestDataFactory.buildGroupTopicWithOffsetDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminReadControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaTaskProvider kafkaTaskProvider;

    @Autowired
    private AdminReadController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ListTopics listTopics;

    @Test
    public void contexLoads() {
        assertThat(controller).isNotNull();
    }

    @BeforeEach
    public void beforeEach(){
        when(kafkaTaskProvider.listTopics(Optional.empty())).thenReturn(listTopics);
    }

    @Test
    public void testTopicList() throws JsonProcessingException, JSONException {
        TopicList expected = expected("Hello World", "Banana");
        when(listTopics.execute()).thenReturn(CompletableFuture.completedFuture(expected));

        String observed = this.restTemplate.getForObject("http://localhost:" + port + "/api/listTopics", String.class);

        JSONAssert.assertEquals(observed, objectMapper.writeValueAsString(expected), NON_EXTENSIBLE);

        verify(listTopics, times(1)).execute();
    }

    @Test
    public void testAdminFailure() throws IOException, JSONException {

        String message = "Failed to gather topic list from kafka";
        CompletableFuture<TopicList> request = new CompletableFuture<>();
        request.completeExceptionally(new KafkaException(message));

        when(listTopics.execute())
                .thenReturn(request);

        String response =  this.restTemplate.getForObject("http://localhost:" + port + "/api/listTopics", String.class);
        ApiError error = objectMapper.readValue(response, ApiError.class);

        assertEquals(KafkaException.class.getName(), error.rootCause());
        assertEquals(message, error.message());

        verify(listTopics, times(1)).execute();
    }

    @Test
    public void testConsumerGroupRequiringUrlEncoding(){
        ConsumerGroupDetailsWithOffset offset = mock(ConsumerGroupDetailsWithOffset.class);
        when(offset.execute()).thenReturn(CompletableFuture.completedFuture(buildGroupTopicWithOffsetDetails().build()));
        when(kafkaTaskProvider.consumerGroupOffsetInformation(eq("kiwi/task/"), eq(Optional.empty())))
                .thenReturn(offset);

        this.restTemplate
                .getForObject("http://localhost:" + port + "/api/listConsumerGroupDetailsWithOffsets/kiwi%2Ftask%2F", String.class);

        verify(kafkaTaskProvider, times(1)).consumerGroupOffsetInformation(eq("kiwi/task/"), eq(Optional.empty()));
    }

    private TopicList expected(String... topics){
        return ImmutableTopicList.builder()
                .addTopics(topics)
                .build();
    }

}
