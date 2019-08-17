package com.github.domwood.kiwi.kafka.utils;

import com.github.domwood.kiwi.data.input.ConsumerStartPosition;
import com.github.domwood.kiwi.data.input.ImmutableConsumerStartPosition;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaOffsetPositionCalculatorTest {

    @Test
    public void testTotalTopicPercentage(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .topicPercentage(0.2)
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, endOffsets, positionTotalPercentage);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 20L,
                topicPartition1, 20L,
                topicPartition2, 20L);

        assertEquals(expected, observed);
    }

    @Test
    public void testTotalTopicCanBe100Percent(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .topicPercentage(1.0)
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, endOffsets, positionTotalPercentage);

        assertEquals(endOffsets, observed);
    }

    @Test
    public void testTotalTopicCanBeNoMoreThan100Percent(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .topicPercentage(2.0)
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, endOffsets, positionTotalPercentage);

        assertEquals(endOffsets, observed);
    }

    @Test
    public void testPerPartitionPercentage(){
        ConsumerStartPosition positionPerPartitionOffsets = ImmutableConsumerStartPosition.builder()
                .percentages(ImmutableMap.of(
                        0, 0.5,
                        1, 0.1,
                        2, 0.2))
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, endOffsets, positionPerPartitionOffsets);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 50L,
                topicPartition1, 10L,
                topicPartition2, 20L);

        assertEquals(expected, observed);
    }


    @Test
    public void testPerPartitionOffsets(){
        ConsumerStartPosition positionPerPartitionOffsets = ImmutableConsumerStartPosition.builder()
                .offsets(ImmutableMap.of(
                        0, 50L,
                        1, 10L,
                        2, 20L))
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, endOffsets, positionPerPartitionOffsets);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 50L,
                topicPartition1, 10L,
                topicPartition2, 20L);

        assertEquals(expected, observed);
    }

    @Test
    public void testOffsetAllZero(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .topicPercentage(0.2)
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, startAtZero, positionTotalPercentage);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 0L,
                topicPartition1, 0L,
                topicPartition2, 0L);

        assertEquals(expected, observed);
    }

    @Test
    public void testEndSameAsStartOffset(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .topicPercentage(0.2)
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(endOffsets, endOffsets, positionTotalPercentage);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 100L,
                topicPartition1, 100L,
                topicPartition2, 100L);

        assertEquals(expected, observed);
    }

    @Test
    public void checkPartitionsOfDifferingSize(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .topicPercentage(0.2)
                .build();

        Map<TopicPartition, Long> input = ImmutableMap.of(
                topicPartition0, 20L,
                topicPartition1, 50L,
                topicPartition2, 200L);

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, input, positionTotalPercentage);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 4L,
                topicPartition1, 10L,
                topicPartition2, 40L);

        assertEquals(expected, observed);
    }

    @Test
    public void checkHandleOffsetsGreaterThanExpected(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .offsets(ImmutableMap.of(
                        0, 150L,
                        1, 10L,
                        2, 20L))
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(startAtZero, endOffsets, positionTotalPercentage);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 100L,
                topicPartition1, 10L,
                topicPartition2, 20L);

        assertEquals(expected, observed);
    }

    @Test
    public void checkHandleNegativeOffsetsGreaterThanExpected(){
        ConsumerStartPosition positionTotalPercentage = ImmutableConsumerStartPosition.builder()
                .topicPercentage(0.2)
                .build();

        Map<TopicPartition, Long> observed =
                KafkaOffsetPositionCalculator.getStartingPositions(endOffsets, startAtZero, positionTotalPercentage);

        Map<TopicPartition, Long> expected = ImmutableMap.of(
                topicPartition0, 0L,
                topicPartition1, 0L,
                topicPartition2, 0L);

        assertEquals(expected, observed);
    }

}
