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
    private final MutableInt totalRecords;

    public KafkaConsumerTracker(Map<TopicPartition, Long> endOffsets, Map<TopicPartition, Long> startOffsets){
        this.endOffsets = endOffsets;
        this.startOffsets = startOffsets;
        this.totalRecords = new MutableInt(0);
    }

    public void incrementRecordCount(){
        this.totalRecords.increment();
    }

    public ConsumerPosition position(KafkaConsumerResource resource){
        Map<TopicPartition, Long> currentPosition = resource.currentPosition(endOffsets.keySet());

        return track(startOffsets, endOffsets, currentPosition, totalRecords.getValue());
    }

    //TODO move to some tracking class
    private ConsumerPosition track(Map<TopicPartition, Long> startOffsets,
                                   Map<TopicPartition, Long> endOffsets,
                                   Map<TopicPartition, Long> currentOffsets,
                                   int totalRecords){
        long start = asTotalOffset(startOffsets);
        long end =  asTotalOffset(endOffsets);
        long position = asTotalOffset(currentOffsets);
        if(end <  position) end = position;

        double positionDiff = (double)Long.max(position - start, 1L);
        double size = (double)Long.max(end - start, 1L);
        double percentage = (positionDiff / size) * 100;

        return ImmutableConsumerPosition.builder()
                .startValue(start)
                .endValue(end)
                .consumerPosition(position)
                .percentage((int)percentage)
                .totalRecords(totalRecords)
                .build();
    }

    private long asTotalOffset(Map<TopicPartition, Long> offsets){
        return offsets.values().stream().reduce(Long::sum).orElse(0L);
    }
}
