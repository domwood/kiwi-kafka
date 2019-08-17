package com.github.domwood.kiwi.kafka.utils;

import com.github.domwood.kiwi.data.output.ConsumerPosition;
import com.github.domwood.kiwi.data.output.ImmutableConsumerPosition;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

public class KafkaConsumerTracker {
    private final Map<TopicPartition, Long> endOffsets;
    private final Map<TopicPartition, Long> startOffsets;
    private final Map<TopicPartition, Long> startingConsumerOffsets;

    private final MutableInt totalRecords;

    public KafkaConsumerTracker(Map<TopicPartition, Long> startOffsets,
                                Map<TopicPartition, Long> endOffsets,
                                Map<TopicPartition, Long> startingConsumerOffsets){
        this.endOffsets = endOffsets;
        this.startOffsets = startOffsets;
        this.startingConsumerOffsets = startingConsumerOffsets;

        this.totalRecords = new MutableInt(0);
    }

    public void incrementRecordCount(){
        this.totalRecords.increment();
    }

    public <K, V> ConsumerPosition position(KafkaConsumerResource<K, V> resource){
        Map<TopicPartition, Long> currentPosition = resource.currentPosition(endOffsets.keySet());

        return track(currentPosition, totalRecords.getValue());
    }

    private ConsumerPosition track(Map<TopicPartition, Long> currentOffsets,
                                   int totalRecords){
        long start = asTotalOffset(startOffsets);
        long end =  asTotalOffset(endOffsets);
        long startingPosition = asTotalOffset(startingConsumerOffsets);
        long currentPosition = asTotalOffset(currentOffsets);
        if(end <  currentPosition) end = currentPosition;

        double skippedOffset = calculatePercentage(start, end, startingPosition);
        double percentageOffset = calculatePercentage(start, end, currentPosition);

        return ImmutableConsumerPosition.builder()
                .startValue(start)
                .endValue(end)
                .consumerPosition(currentPosition)
                .percentage((int)percentageOffset)
                .totalRecords(totalRecords)
                .skippedPercentage((int)skippedOffset)
                .build();
    }

    private long asTotalOffset(Map<TopicPartition, Long> offsets){
        return offsets.values().stream().reduce(Long::sum).orElse(0L);
    }

    private double calculatePercentage(Long start, Long end, Long position){
        double positionDiff = calculatePositionDiff(position, start);
        double positionSize = calculateSize(start, end);
        return calculatePercentage(positionDiff, positionSize);
    }

    private double calculatePositionDiff(Long position, Long start){
        return Math.max(position - start, 0L);
    }

    private double calculateSize(Long start, Long end){
        return end - start;
    }

    private double calculatePercentage(Double positionDiff, Double size){
        if(size == 0) return 100;
        return (positionDiff / size) * 100;
    }

    public Map<TopicPartition, Long> getEndOffsets() {
        return endOffsets;
    }

    public Map<TopicPartition, Long> getStartOffsets() {
        return startOffsets;
    }

    public Map<TopicPartition, Long> getStartingConsumerOffsets() {
        return startingConsumerOffsets;
    }
}
