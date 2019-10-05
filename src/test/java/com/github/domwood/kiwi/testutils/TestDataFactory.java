package com.github.domwood.kiwi.testutils;

import com.github.domwood.kiwi.data.input.*;
import com.github.domwood.kiwi.data.output.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class TestDataFactory {

    public static final String testTopic = "aTestTopic";
    public static final String testKey = "testKey";
    public static final String testPayload = "{\"key\":\"value\"}";
    public static final Map<String, String> testHeaders = ImmutableMap.of("TestHeaderKey", "TestHeaderValue");
    public static final Integer replicationFactor = 1;
    public static final Integer partitionCount = 7;
    public static final Long testOffset = 11L;
    public static final Integer testPartition = 3;
    public static final Long testTimestamp = -1L;
    public static final SortedMap<String, String> topicConfiguration = ImmutableSortedMap
            .of("cleanup.policy", "compact",
                "segment.ms", "18000000");
    public static final String testClientId = "kiwiTestClient";

    public static ImmutableCreateTopicRequest.Builder createTopicRequest(String name){
        return ImmutableCreateTopicRequest
                .builder()
                .replicationFactor(replicationFactor)
                .partitions(partitionCount)
                .name(name)
                .configuration(topicConfiguration);
    }

    public static ImmutableProducerRequest.Builder buildProducerRequest(){
        return buildProducerRequest(testTopic);
    }

    public static ImmutableProducerRequest.Builder buildProducerRequest(String topic){
        return ImmutableProducerRequest.builder()
                .topic(topic)
                .key(testKey)
                .payload(testPayload)
                .headers(testHeaders);
    }

    public static ImmutableConsumerRequest.Builder buildConsumerRequest(){
        return buildConsumerRequest(testTopic);
    }

    public static ImmutableConsumerRequest.Builder buildConsumerRequest(String topic){
        return buildConsumerRequest(topic, 1);
    }

    public static ImmutableConsumerRequest.Builder buildConsumerRequest(String topic, int limit){
        return ImmutableConsumerRequest.builder()
                .topics(singletonList(topic))
                .limit(limit);
    }

    public static ImmutableConsumerToFileRequest.Builder buildConsumerToFileRequest(ConsumerRequestFileType type, String delimiter, ConsumerRequestColumns... columns){
        return ImmutableConsumerToFileRequest.builder()
                .topics(singletonList(testTopic))
                .limit(-1)
                .fileType(type)
                .addColumns(columns)
                .columnDelimiter(Optional.ofNullable(delimiter));
    }

    public static ImmutableTopicInfo.Builder buildTopicInfo(String topicName){
        return ImmutableTopicInfo.builder()
                .partitionCount(partitionCount)
                .replicaCount(replicationFactor)
                .topic(topicName)
                .configuration(topicConfiguration)
                .partitions(IntStream.range(0, partitionCount).boxed().map((i) -> TestDataFactory.buildPartitionInfo(i).build()).collect(toList()));
    }

    public static ImmutablePartitionInfo.Builder buildPartitionInfo(Integer index){
        return ImmutablePartitionInfo.builder()
                .leader(TestKafkaServer.testBrokerId)
                .replicas(singletonList(TestKafkaServer.testBrokerId))
                .isrs(singletonList(TestKafkaServer.testBrokerId))
                .partition(index)
                .replicationFactor(replicationFactor);
    }

    public static BrokerInfoList buildBrokerInfoList(){
        return ImmutableBrokerInfoList.builder()
                .addBrokerInfo(buildBrokerInfo()
                        .build())
                .build();
    }

    private static ImmutableBrokerInfo.Builder buildBrokerInfo(){
        return ImmutableBrokerInfo.builder()
                .nodeName(String.valueOf(TestKafkaServer.testBrokerId))
                .nodeNumber(TestKafkaServer.testBrokerId)
                .host(TestKafkaServer.kafkaHost)
                .port(TestKafkaServer.kafkaPort)
                .nodeRack(null);
    }

    public static ImmutableConsumerResponse.Builder<String, String> buildConsumerResponse(){
        return ImmutableConsumerResponse.<String, String>builder()
                .messages(asList(buildConsumedMessage().build()));
    }

    public static ImmutableConsumedMessage.Builder<String, String> buildConsumedMessage(){
        return ImmutableConsumedMessage.<String, String>builder()
                .key(testKey)
                .headers(testHeaders)
                .message(testPayload)
                .offset(testOffset)
                .partition(testPartition)
                .timestamp(testTimestamp);
    }

    public static ImmutableConsumerPosition.Builder buildConsumerPosition(){
        return ImmutableConsumerPosition.builder()
                .startValue(0L)
                .endValue(1L)
                .consumerPosition(1L)
                .percentage(100)
                .totalRecords(1);
    }

    public static ImmutableProducerResponse.Builder buildProducerResponse() {
        return ImmutableProducerResponse.builder()
                .offset(testOffset)
                .partition(testPartition)
                .topic(testTopic);
    }

    public static ImmutableConsumerGroupTopicWithOffsetDetails.Builder buildGroupTopicWithOffsetDetails(){
        return ImmutableConsumerGroupTopicWithOffsetDetails.builder()
                .putOffset(testTopic, ImmutableList.of(buildTopicGroupAssignmentWithOffset().build()));
    }

    public static ImmutableTopicGroupAssignmentWithOffset.Builder buildTopicGroupAssignmentWithOffset(){
        return ImmutableTopicGroupAssignmentWithOffset.builder()
                .clientId(testClientId)
                .groupId(testClientId)
                .offset(buildPartitionOffset().build())
                .partition(testPartition)
                .topic(testTopic)
                .coordinator("0")
                .groupState("UNKNOWN");
    }

    public static ImmutablePartitionOffset.Builder buildPartitionOffset(){
        return ImmutablePartitionOffset.builder()
                .groupOffset(testOffset)
                .partitionOffset(testOffset)
                .lag(0L);
    }

    public static TopicPartition topicPartition0 = new TopicPartition("test", 0);
    public static TopicPartition topicPartition1 = new TopicPartition("test", 1);
    public static TopicPartition topicPartition2 = new TopicPartition("test", 2);

    public static Map<TopicPartition, Long> startAtZero = ImmutableMap.of(
            topicPartition0, 0L,
            topicPartition1, 0L,
            topicPartition2, 0L
    );

    public static Map<TopicPartition, Long> middleOffset = ImmutableMap.of(
            topicPartition0, 50L,
            topicPartition1, 50L,
            topicPartition2, 50L
    );

    public static Map<TopicPartition, Long> endOffsets = ImmutableMap.of(
            topicPartition0, 100L,
            topicPartition1, 100L,
            topicPartition2, 100L
    );

    public static Map<TopicPartition, Long> beyondEndOffsets = ImmutableMap.of(
            topicPartition0, 200L,
            topicPartition1, 200L,
            topicPartition2, 200L
    );

}
