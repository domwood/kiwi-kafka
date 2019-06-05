package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.output.ImmutableTopicList;
import com.github.domwood.kiwi.data.output.TopicList;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.admin.ListTopics;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaTaskProvider kafkaTaskProvider;

    @Autowired
    private AdminController controller;

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

    private TopicList expected(String... topics){
        return ImmutableTopicList.builder()
                .addTopics(topics)
                .build();
    }

}
