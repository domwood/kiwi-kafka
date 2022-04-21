package com.github.domwood.kiwi.kafka.utils;

import com.github.domwood.kiwi.data.output.ConsumerPosition;
import com.github.domwood.kiwi.data.output.ImmutableConsumerPosition;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.IntStream;

import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerTrackerTest {

    @Mock
    KafkaConsumerResource<String, String> consumerResource;

    @BeforeEach
    public void beforeEach(){
        when(consumerResource.currentPosition(anySet()))
                .thenReturn(middleOffset);
    }

    @Test
    public void testStartingTracker(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(startAtZero, endOffsets , startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(startAtZero);

        ConsumerPosition observed = consumerTracker.gatherUpdatedPosition(consumerResource).getRight();

        ConsumerPosition expected = position(0, 300, 0, 0, 0);

        assertEquals(expected, observed);
    }


    @Test
    public void testMiddleTracking(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(startAtZero, endOffsets , startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(middleOffset);

        ConsumerPosition observed = consumerTracker.gatherUpdatedPosition(consumerResource).getRight();

        ConsumerPosition expected = position(0, 300, 150, 50, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void testEndTracking(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(startAtZero, endOffsets , startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        ConsumerPosition observed = consumerTracker.gatherUpdatedPosition(consumerResource).getRight();

        ConsumerPosition expected = position(0, 300, 300, 100, 0);

        assertEquals(expected, observed);
    }


    @Test
    public void testBeyondEndTracking(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(startAtZero, endOffsets , startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(beyondEndOffsets);

        ConsumerPosition observed = consumerTracker.gatherUpdatedPosition(consumerResource).getRight();

        ConsumerPosition expected = position(0, 600, 600, 100, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void testMultipleUpdates(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(startAtZero, endOffsets , startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(startAtZero);

        consumerTracker.gatherUpdatedPosition(consumerResource);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        ConsumerPosition observed = consumerTracker.gatherUpdatedPosition(consumerResource).getRight();

        ConsumerPosition expected = position(0, 300, 300, 100, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void startAtEnd(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(endOffsets, endOffsets, endOffsets);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        ConsumerPosition observed = consumerTracker.gatherUpdatedPosition(consumerResource).getRight();

        ConsumerPosition expected = position(300, 300, 300, 100, 0, 0);

        assertEquals(expected, observed);
    }

    @Test
    public void testRecords(){
        KafkaConsumerTracker consumerTracker = new KafkaConsumerTracker(startAtZero, endOffsets , startAtZero);

        when(consumerResource.currentPosition(anySet()))
                .thenReturn(endOffsets);

        IntStream.range(0, 56).forEach(i -> consumerTracker.incrementRecordCount());

        ConsumerPosition observed = consumerTracker.gatherUpdatedPosition(consumerResource).getRight();

        ConsumerPosition expected = position(0, 300, 300, 100, 56);

        assertEquals(expected, observed);
    }

    private ConsumerPosition position(long start, long end, long position, int percentage, int records){
        return position(start, end, position, percentage, records, 0);
    }

    private ConsumerPosition position(long start, long end, long position, double percentage, int records, double skipped){
        return ImmutableConsumerPosition.builder()
                .startValue(start)
                .endValue(end)
                .consumerPosition(position)
                .percentage(percentage)
                .totalRecords(records)
                .skippedPercentage(skipped)
                .build();
    }
}
