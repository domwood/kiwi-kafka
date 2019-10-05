package com.github.domwood.kiwi.kafka.task.consumer;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.ImmutableConsumerRequest;
import com.github.domwood.kiwi.data.input.filter.FilterApplication;
import com.github.domwood.kiwi.data.input.filter.FilterType;
import com.github.domwood.kiwi.data.input.filter.ImmutableMessageFilter;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableConsumedMessage;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BasicConsumeMessagesTest {

    private static final String KEY_VALUE = "KEY_%s";
    private static final String RECORD_VALUE = "VALUE_%s";

    @Mock
    KafkaConsumerResource<String, String> consumerResource;

    private void setupMock(int partitionCount, int perPartitionSize, int recordsPerPoll){

        setupAssignment(partitionCount, perPartitionSize);

        OngoingStubbing<ConsumerRecords<String, String>> stub = when(consumerResource.poll(any(Duration.class)));
        for(int i=0; i < perPartitionSize; i+=recordsPerPoll){
            stub = stub.thenReturn(consumerRecords(partitionCount, recordsPerPoll, i+1));
        }

        stub.thenReturn(emptyRecords());
    }

    private void setupAssignment(int partitionCount, int perPartitionSize){
        reset(consumerResource);

        when(consumerResource.assignment())
                .thenReturn(assignment(partitionCount));

        when(consumerResource.endOffsets(any(Set.class)))
                .thenReturn(endOffsets(partitionCount, perPartitionSize));
    }

    @DisplayName("Basic test that message consuming works")
    @Test
    public void testConsumeMessages() throws InterruptedException, ExecutionException, TimeoutException {
        setupMock(2, 3, 1);

        BasicConsumeMessages basicConsumeMessages = new BasicConsumeMessages(consumerResource, buildConsumerRequest(testTopic, 100).build());

        ConsumerResponse<String, String> consumerResponse =
                basicConsumeMessages.execute().get(20, TimeUnit.SECONDS);

        assertEquals(6, consumerResponse.messages().size());

        List<ConsumedMessage<String, String>> expected = buildConsumedMessages(2, 3);
        assertEquals(consumerResponse.messages(), expected);
    }

    @DisplayName("Test that limits are applied after filter checks")
    @Test
    public void testFiltersAppliedBeforeLimitCheck() throws InterruptedException, ExecutionException, TimeoutException {
        setupMock(2, 3, 1);

        ConsumerRequest request = ImmutableConsumerRequest.builder()
                .from(buildConsumerRequest(testTopic, 2).build())
                .filters(asList(ImmutableMessageFilter
                        .builder()
                        .filter(String.format(KEY_VALUE, 1))
                        .filterType(FilterType.MATCHES)
                        .isCaseSensitive(true)
                        .filterApplication(FilterApplication.KEY)
                        .build()))
                .build();

        BasicConsumeMessages basicConsumeMessages = new BasicConsumeMessages(consumerResource, request);

        ConsumerResponse<String, String> consumerResponse =
                basicConsumeMessages.execute().get(20, TimeUnit.SECONDS);

        assertEquals(2, consumerResponse.messages().size());

        List<ConsumedMessage<String, String>> expected = buildConsumedMessages(2, 1);

        assertEquals(consumerResponse.messages(), expected);
    }

    @DisplayName("Test that failure is handled")
    @Test
    public void failureTest(){

        setupAssignment(2, 3);

        when(consumerResource.poll(any(Duration.class)))
                .thenThrow(new KafkaException("Failed to do kafka thing"));

        BasicConsumeMessages basicConsumeMessages = new BasicConsumeMessages(consumerResource, buildConsumerRequest().build());

        CompletableFuture<ConsumerResponse<String, String>> future = basicConsumeMessages.execute();

        await().atMost(1, TimeUnit.SECONDS)
                .until(future::isCompletedExceptionally);

        assertThrows(ExecutionException.class, future::get);
    }

    private List<ConsumedMessage<String, String>> buildConsumedMessages(int partitions, int records){
        return IntStream.range(1, records+1).boxed()
                .flatMap(i -> IntStream.range(0, partitions).boxed()
                        .map(p -> buildConsumedMessage(p, i)))
                .collect(toList());
    }

    private ConsumedMessage<String, String> buildConsumedMessage(int partition, int i){
        return ImmutableConsumedMessage.<String, String>builder()
                .key(String.format(KEY_VALUE, i))
                .headers(emptyMap())
                .message(String.format(RECORD_VALUE, i))
                .offset(i)
                .partition(partition)
                .timestamp(testTimestamp)
                .build();
    }

    private ConsumerRecords<String, String> emptyRecords(){
        return new ConsumerRecords<>(emptyMap());
    }

    private ConsumerRecords<String, String> consumerRecords(int partitionCount,
                                                            int perPartitionCount,
                                                            int startingOffset){
        return new ConsumerRecords<>(IntStream.range(0, partitionCount)
                .boxed()
                .collect(toMap(p -> new TopicPartition(testTopic, p), p -> consumerRecordList(perPartitionCount, p, startingOffset))));
    }

    private List<ConsumerRecord<String, String>> consumerRecordList(int recordListSize, int partition, int startingOffset){
        return IntStream.range(startingOffset, startingOffset+recordListSize)
                .boxed()
                .map(i -> consumerRecord(partition, i))
                .collect(toList());
    }

    private ConsumerRecord<String, String> consumerRecord(int partition, int i){
        return new ConsumerRecord<>(testTopic, partition, i, String.format(KEY_VALUE, i), String.format(RECORD_VALUE, i));
    }

    private Set<TopicPartition> assignment(int partitions){
        return IntStream.range(0, partitions)
                .boxed()
                .map(i -> new TopicPartition(testTopic, i))
                .collect(toSet());
    }

    private Map<TopicPartition, Long> endOffsets(int partitions, long size){
        return IntStream.range(0, partitions)
                .boxed()
                .map(i -> new TopicPartition(testTopic, i))
                .collect(toMap(tp -> tp, tp -> size + 1L));
    }

}
