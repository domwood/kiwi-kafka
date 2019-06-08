package com.github.domwood.kiwi.testutils;

import com.github.domwood.kiwi.data.input.*;
import com.github.domwood.kiwi.data.output.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

import java.util.Map;
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

    public static CreateTopicRequest createTopicRequest(String name){
        return ImmutableCreateTopicRequest
                .builder()
                .replicationFactor(replicationFactor)
                .partitions(partitionCount)
                .name(name)
                .configuration(topicConfiguration)
                .build();
    }

    public static ProducerRequest buildProducerRequest(){
        return buildProducerRequest(testTopic);
    }

    public static ProducerRequest buildProducerRequest(String topic){
        return ImmutableProducerRequest.builder()
                .topic(topic)
                .key(testKey)
                .payload(testPayload)
                .headers(testHeaders)
                .build();
    }

    public static ConsumerRequest buildConsumerRequest(){
        return buildConsumerRequest(testTopic);
    }

    public static ConsumerRequest buildConsumerRequest(String topic){
        return buildConsumerRequest(topic, false, 1);
    }

    public static ConsumerRequest buildConsumerRequest(String topic, boolean appliesFromStart, int limit){
        return ImmutableConsumerRequest.builder()
                .topics(singletonList(topic))
                .limit(limit)
                .limitAppliesFromStart(appliesFromStart)
                .build();
    }

    public static TopicInfo buildTopicInfo(String topicName){
        return ImmutableTopicInfo.builder()
                .partitionCount(partitionCount)
                .replicaCount(replicationFactor)
                .topic(topicName)
                .configuration(topicConfiguration)
                .partitions(IntStream.range(0, partitionCount).boxed().map(TestDataFactory::buildPartitionInfo).collect(toList()))
                .build();
    }

    public static PartitionInfo buildPartitionInfo(Integer index){
        return ImmutablePartitionInfo.builder()
                .leader(TestKafkaServer.testBrokerId)
                .replicas(singletonList(TestKafkaServer.testBrokerId))
                .isrs(singletonList(TestKafkaServer.testBrokerId))
                .partition(index)
                .replicationFactor(replicationFactor)
                .build();
    }

    public static BrokerInfoList buildBrokerInfoList(){
        return ImmutableBrokerInfoList.builder()
                .addBrokerInfo(ImmutableBrokerInfo.builder()
                        .nodeName(String.valueOf(TestKafkaServer.testBrokerId))
                        .nodeNumber(TestKafkaServer.testBrokerId)
                        .host(TestKafkaServer.kafkaHost)
                        .port(TestKafkaServer.kafkaPort)
                        .nodeRack(null)
                        .build())
                .build();
    }

    public static ConsumerResponse<String, String> buildConsumerResponse(){
        return ImmutableConsumerResponse.<String, String>builder()
                .messages(asList(ImmutableConsumedMessage.<String, String>builder()
                        .key(testKey)
                        .headers(testHeaders)
                        .message(testPayload)
                        .offset(testOffset)
                        .partition(testPartition)
                        .timestamp(testTimestamp)
                        .build()))
                .build();
    }

    public static ProducerResponse buildProducerResponse() {
        return ImmutableProducerResponse.builder()
                .offset(testOffset)
                .partition(testPartition)
                .topic(testTopic)
                .build();
    }

}
