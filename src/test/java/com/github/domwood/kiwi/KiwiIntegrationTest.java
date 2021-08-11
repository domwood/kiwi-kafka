package com.github.domwood.kiwi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.error.ApiError;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.CreateTopicRequest;
import com.github.domwood.kiwi.data.input.ImmutableCloseTaskRequest;
import com.github.domwood.kiwi.data.input.ProducerRequest;
import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.testutils.TestKafkaServer;
import com.github.domwood.kiwi.testutils.TestWebSocketClient;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.domwood.kiwi.testutils.HttpTestUtils.asJsonPayload;
import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@ExtendWith({TestKafkaServer.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KiwiIntegrationTest {

    private static final int defaultTimeoutMs = 5000;

    @LocalServerPort
    private Integer serverPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper mapper;

    private String topicInfoUrl;
    private String topicListUrl;
    private String topicDeleteUrl;
    private String topicCreateUrl;
    private String produceMessageUrl;
    private String consumeMessageUrl;

    @BeforeEach
    private void setupUrls(){
        this.topicInfoUrl = "http://localhost:"+serverPort+"/api/topicInfo/";
        this.topicListUrl = "http://localhost:"+serverPort+"/api/listTopics";
        this.topicDeleteUrl = "http://localhost:"+serverPort+"/api/deleteTopic/";
        this.topicCreateUrl = "http://localhost:"+serverPort+"/api/createTopic";
        this.produceMessageUrl = "http://localhost:"+serverPort+"/api/produce";
        this.consumeMessageUrl = "http://localhost:"+serverPort+"/api/consume";
    }

    @DisplayName("Test querying a topic that doesn't exist returns 404")
    @Test
    public void handleTopicDoesntExist(){
        ResponseEntity<ApiError> requestError =
                awaitErrorData(topicInfoUrl + "missing", ApiError.class);

        assertEquals(HttpStatus.NOT_FOUND, requestError.getStatusCode());
        assertEquals(UnknownTopicOrPartitionException.class.getName(), requestError.getBody().rootCause());
    }

    @DisplayName("Test can create a topic on a kafka broker")
    @Test
    public void topicCreationTest(){
        String createdTestTopic = "createATestTopic";

        createTopicAndAwait(createdTestTopic);

        ResponseEntity<TopicInfo> topicInfoRequest =
               awaitData(topicInfoUrl+"createATestTopic", TopicInfo.class);

        TopicInfo expected = buildTopicInfo(createdTestTopic)
                .configuration(topicInfoRequest.getBody().configuration()) //Default configs defined by server
                .build();

        assertEquals(expected, topicInfoRequest.getBody());
    }

    @DisplayName("Test can delete a topic on a kafka broker")
    @Test
    public void topicDeleteTest(){
        String createdTestTopic = "createATopicToDelete";

        createTopicAndAwait(createdTestTopic);

        testRestTemplate.delete(topicDeleteUrl+createdTestTopic);

        awaitAndReturn(
                () -> awaitData(topicListUrl, TopicList.class),
                (ResponseEntity<TopicList> data) -> ! data.getBody().topics().contains(createdTestTopic));
    }

    @DisplayName("Test created topic will then be returned in a topic list query")
    @Test
    public void topicListTest(){
        String listableTopic = "topicShouldAppearInList";

        createTopicAndAwait(listableTopic);

        awaitAndReturn(
                () -> awaitData(topicListUrl, TopicList.class),
                (response) -> response.getBody().topics().contains(listableTopic));
    }

    @DisplayName("Test correctly produce and consume a message from kafka")
    @SuppressWarnings("unchecked")
    @Test
    public void readWriteTest(){
        String readWriteTestTopic = "readWriteTest";

        createTopicAndAwait(readWriteTestTopic);

        ProducerRequest produce = buildProducerRequest(readWriteTestTopic).build();

        ResponseEntity<ProducerResponse> producerResponse = produceMessage(produce);

        assertEquals(readWriteTestTopic, producerResponse.getBody().topic());

        ConsumerRequest consumerRequest = buildConsumerRequest(readWriteTestTopic).build();

        ResponseEntity<ConsumerResponse> consumerResponse = consumeMessages(consumerRequest, 1);

        ConsumedMessage<String, String> message = (ConsumedMessage<String, String>) consumerResponse.getBody().messages().get(0);

        assertEquals(testKey, message.key());
        assertEquals(testPayload, message.message());
        assertEquals(testHeaders, message.headers());
    }

    @DisplayName("Test correctly produce and consume a message from kafka for null values")
    @SuppressWarnings("unchecked")
    @Test
    public void readWriteTestNullValuesTest(){
        String readWriteTestTopic = "readWriteNullValuesTest";

        createTopicAndAwait(readWriteTestTopic);

        ProducerRequest produce =
                buildProducerRequest(readWriteTestTopic)
                        .payload(Optional.empty())
                .build();

        ResponseEntity<ProducerResponse> producerResponse = produceMessage(produce);

        ConsumerRequest consumerRequest = buildConsumerRequest(readWriteTestTopic).build();

        ResponseEntity<ConsumerResponse> consumerResponse = consumeMessages(consumerRequest, 1);

        assertNotNull(consumerResponse.getBody());
        assertEquals(1, consumerResponse.getBody().messages().size());

        ConsumedMessage<String, String> message = (ConsumedMessage<String, String>) consumerResponse.getBody().messages().get(0);

        assertEquals(testKey, message.key());
        assertEquals(null, message.message());
        assertEquals(testHeaders, message.headers());
    }

    @DisplayName("Test Broker Information Returned")
    @Test
    public void brokerInfoTest(){
        ResponseEntity<BrokerInfoList> brokerInfo =
                testRestTemplate.getForEntity("http://localhost:"+serverPort+"/api/brokers", BrokerInfoList.class);

        assertTrue(brokerInfo.getStatusCode().is2xxSuccessful());

        BrokerInfoList expected = buildBrokerInfoList();
        assertEquals(expected, brokerInfo.getBody());
    }

    @DisplayName("Test Websocket connection initially returns a starting position before any data")
    @Test
    public void testWebsocketReturnsInitialPosition() throws IOException {
        String testWebSocketTopic = "testWebSocketInitialPosition";

        createTopicAndAwait(testWebSocketTopic);

        ConsumerResponse<String, String> initialPosition = consumeMessageWebsocket(buildConsumerRequest(testWebSocketTopic).build());

        assertEquals(0, initialPosition.position().get().totalRecords().intValue());
        assertEquals(0, initialPosition.messages().size());
    }

    @DisplayName("Test Websocket connection can forward data consumed from kafka")
    @Test
    public void testWebSocketEndpoint() throws IOException {
        String testWebSocketTopic = "testWebSocketTopic";

        createTopicAndAwait(testWebSocketTopic);

        produceMessage(buildProducerRequest(testWebSocketTopic).build());

        ConsumerResponse<String, String> observed = consumeMessageWebsocket(buildConsumerRequest(testWebSocketTopic).build(), 2).get(1);

        ConsumerResponse expected = ImmutableConsumerResponse.<String, String>builder()
                .addMessage(buildConsumedMessage()
                        .timestamp(observed.messages().get(0).timestamp())//Can't really guarantee these values
                        .offset(0)
                        .build())
                .position(buildConsumerPosition().build())
                .build();

        assertEquals(expected, observed);
    }

    @DisplayName("Test Websocket connection can forward data consumed from kafka with null values")
    @Test
    public void testWebSocketEndpointHandlesNullValues() throws IOException {
        String testWebSocketTopic = "testWebSocketTopicNullValues";

        createTopicAndAwait(testWebSocketTopic);

        produceMessage(buildProducerRequest(testWebSocketTopic)
                .payload(Optional.empty())
                .build());

        ConsumerResponse<String, String> observed = consumeMessageWebsocket(buildConsumerRequest(testWebSocketTopic).build(), 2).get(1);

        ConsumerResponse expected = ImmutableConsumerResponse.<String, String>builder()
                .addMessage(buildConsumedMessage()
                        .timestamp(observed.messages().get(0).timestamp())//Can't really guarantee these values
                        .offset(0)
                        .message(null)
                        .build())
                .position(buildConsumerPosition().build())
                .build();

        assertEquals(expected, observed);
    }

    private ConsumerResponse<String, String> consumeMessageWebsocket(ConsumerRequest request){
        return consumeMessageWebsocket(request, 1).get(0);
    }

    private List<ConsumerResponse<String, String>> consumeMessageWebsocket(ConsumerRequest request, int count){
        List<ConsumerResponse<String, String>> data = new ArrayList<>();
        TestWebSocketClient testWebSocketClient = new TestWebSocketClient();
        testWebSocketClient.connect( "ws://localhost:"+serverPort+"/ws");

        await().atMost(10, TimeUnit.SECONDS).until(testWebSocketClient::isOpen);

        try {
            String message = this.mapper.writeValueAsString(request);
            testWebSocketClient.send(message);

            await().atMost(10, TimeUnit.SECONDS).until(() -> testWebSocketClient.getReceived().size() >= count);

            while(!testWebSocketClient.getReceived().isEmpty() && data.size() < count){
                String payload = testWebSocketClient.getReceived().poll().getPayload();
                data.add(mapper.readValue(payload, ConsumerResponse.class));
            }

            String close = mapper.writeValueAsString(ImmutableCloseTaskRequest.builder().closeSession(true).build());
            testWebSocketClient.send(close);

            await().atMost(10, TimeUnit.SECONDS).until(() -> !testWebSocketClient.isOpen());
        }
        catch (IOException e){
            throw new AssertionError("Websocket data failed to be sent " + request);
        }
        assertEquals(count, data.size(), "Expected websocket data returned to contain " + count + " elements but contains " + data.size());
        return data;
    }

    private <T> ResponseEntity<T> awaitAndReturn(Supplier<ResponseEntity<T>> supplier, Predicate<ResponseEntity<T>> asserter){
        AtomicReference<ResponseEntity<T>> latestResponse = new AtomicReference<>(null);
        await().atMost(defaultTimeoutMs, MILLISECONDS).until(() -> {
            try{
                ResponseEntity<T> response = supplier.get();
                latestResponse.set(response);
                return asserter.test(response);
            }
            catch (Exception e){
                return false;
            }
        });

        return latestResponse.get();
    }

    private ResponseEntity<ProducerResponse> produceMessage(ProducerRequest request){
        ResponseEntity<ProducerResponse> producerResponse =
                testRestTemplate.postForEntity(produceMessageUrl, asJsonPayload(request), ProducerResponse.class);
        assertTrue(producerResponse.getStatusCode().is2xxSuccessful() &&
                producerResponse.getBody().topic().equals(request.topic()));
        return producerResponse;
    }

    private ResponseEntity<ConsumerResponse> consumeMessages(ConsumerRequest request, int count){
        return awaitAndReturn(
                () -> testRestTemplate.postForEntity(this.consumeMessageUrl, asJsonPayload(request), ConsumerResponse.class),
                (response) -> response.getBody() != null &&
                        response.getStatusCode().is2xxSuccessful() &&
                        response.getBody().messages().size() >= count);
    }

    private void createTopicAndAwait(String createdTestTopic){
        CreateTopicRequest topicCreateRequest = createTopicRequest(createdTestTopic).build();

        ResponseEntity<Void> topicCreationResponse =
                testRestTemplate.postForEntity(topicCreateUrl, asJsonPayload(topicCreateRequest), Void.class);

        assertTrue(topicCreationResponse.getStatusCode().is2xxSuccessful());

        awaitAndReturn(
                () -> testRestTemplate.getForEntity(topicListUrl, TopicList.class),
                (response) -> response.getStatusCode().is2xxSuccessful() &&
                                response.getBody().topics().contains(createdTestTopic));
    }

    private <T> ResponseEntity<T> awaitData(String url, Class<T> dataType){
        return awaitAndReturn(
                () -> testRestTemplate.getForEntity(url, dataType),
                (response) -> response.getStatusCode().is2xxSuccessful());
    }

    private <T> ResponseEntity<T> awaitErrorData(String url, Class<T> dataType){
        return awaitAndReturn(
                () -> testRestTemplate.getForEntity(url, dataType),
                (response) -> response.getStatusCode().isError());
    }
}
