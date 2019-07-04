package com.github.domwood.kiwi.kafka.utils;

import com.github.domwood.kiwi.data.output.ConsumerPosition;
import com.github.domwood.kiwi.data.output.ImmutableConsumerPosition;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerTrackerTest {

    TopicPartition topicPartition1 = new TopicPartition("test", 0);
    TopicPartition topicPartition2 = new TopicPartition("test", 1);
    TopicPartition topicPartition3 = new TopicPartition("test", 2);

    Map<TopicPartition, Long> startAtZero = ImmutableMap.of(
            topicPartition1, 0L,
            topicPartition2, 0L,
            topicPartition3, 0L
    );

    Map<TopicPartition, Long> middleOffset = ImmutableMap.of(
            topicPartition1, 50L,
            topicPartition2, 50L,
            topicPartition3, 50L
    );

    Map<TopicPartition, Long> endOffsets = ImmutableMap.of(
            topicPartition1, 100L,
            topicPartition2, 100L,
            topicPartition3, 100L
    );

    Map<TopicPartition, Long> beyondEndOffsets = ImmutableMap.of(
            topicPartition1, 200L,
            topicPartition2, 200L,
            topicPartition3, 200L
    );

    @Mock
    KafkaConsumerResource<String, String> consumerResource;

    @BeforeEach
    public void beforeEach(){
        when(consumerResource.currentPosition(anySet()))
                .thenReturn(middleOffset);
    }

    @Test
    public void testStartingTracker(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(startAtZero);

        ConsumerPosition observed = consumerTracker.position(consumerResource);

        ConsumerPosition expected = position(0, 300, 0, 0, 0);

        assertEquals(expected, observed);
    }


    @Test
    public void testMiddleTracking(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(middleOffset);

        ConsumerPosition observed = consumerTracker.position(consumerResource);

        ConsumerPosition expected = position(0, 300, 150, 50, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void testEndTracking(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        ConsumerPosition observed = consumerTracker.position(consumerResource);

        ConsumerPosition expected = position(0, 300, 300, 100, 0);

        assertEquals(expected, observed);
    }


    @Test
    public void testBeyondEndTracking(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(beyondEndOffsets);

        ConsumerPosition observed = consumerTracker.position(consumerResource);

        ConsumerPosition expected = position(0, 600, 600, 100, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void testMultipleUpdates(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(startAtZero);

        consumerTracker.position(consumerResource);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        ConsumerPosition observed = consumerTracker.position(consumerResource);

        ConsumerPosition expected = position(0, 300, 300, 100, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void startAtEnd(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, endOffsets);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        ConsumerPosition observed = consumerTracker.position(consumerResource);

        ConsumerPosition expected = position(300, 300, 300, 100, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void testRecords(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        IntStream.range(0, 56).forEach(i -> consumerTracker.incrementRecordCount());

        ConsumerPosition observed = consumerTracker.position(consumerResource);

        ConsumerPosition expected = position(0, 300, 300, 100, 56);

        assertEquals(expected, observed);
    }

    private ConsumerPosition position(long start, long end, long position, int percentage, int records){
        return ImmutableConsumerPosition.builder()
                .startValue(start)
                .endValue(end)
                .consumerPosition(position)
                .percentage(percentage)
                .totalRecords(records)
                .build();
    }
}
