package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ImmutableTopicList;
import com.github.domwood.kiwi.data.output.TopicList;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListTopicsTest {

    @Mock
    KafkaAdminResource resource;

    @Mock
    KafkaFuture<Collection<TopicListing>> future;

    @Mock
    ListTopicsResult result;

    List<TopicListing> topicListings = new ArrayList<>();

    @BeforeEach
    public void beforeEach() {
        topicListings.clear();

        when(resource.listTopics()).thenReturn(result);
        when(result.listings()).thenReturn(future);

    }

    private void addTopic(String... topics){
        for(String topic : topics){
            this.topicListings.add(new TopicListing(topic, false));
        }
    }

    private TopicList createExpected(String... topics){
        return ImmutableTopicList.builder()
                .addTopics(topics)
                .build();
    }

    @DisplayName("Returns a list of topics")
    @Test
    public void testListTopics() throws InterruptedException, ExecutionException, TimeoutException {
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(topicListings);

        addTopic("test1", "test2");

        ListTopics listTopics = new ListTopics(resource, null);

        TopicList observed = listTopics.execute().get(10, TimeUnit.SECONDS);

        TopicList expected = createExpected("test1", "test2");

        assertEquals(expected, observed);
    }

    @DisplayName("Handles failure when retrieving list of topics")
    @Test
    public void testListTopicsFailure() throws InterruptedException, ExecutionException, TimeoutException {

        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new KafkaException("Failed to get topic list"));

        ListTopics listTopics = new ListTopics(resource, null);

        CompletableFuture<TopicList> observed = listTopics.execute();

        await().atMost(1, TimeUnit.SECONDS)
                .until(observed::isCompletedExceptionally);

        assertThrows(ExecutionException.class, observed::get);
    }

    @DisplayName("Returned list is ordered alphabetically")
    @Test
    public void ordersAlphabetically() throws InterruptedException, ExecutionException, TimeoutException {
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(topicListings);

        addTopic(
                "canada",
                "banana",
                "apple",
                "zebra",
                "000000",
                "Cucumber"
        );

        ListTopics listTopics = new ListTopics(resource, null);

        TopicList observed = listTopics.execute().get(10, TimeUnit.SECONDS);

        TopicList expected = createExpected(
                "000000",
                "Cucumber",
                "apple",
                "banana",
                "canada",
                "zebra"
        );

        assertEquals(expected, observed);
    }

}
