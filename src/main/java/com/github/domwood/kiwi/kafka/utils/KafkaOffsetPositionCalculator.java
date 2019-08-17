package com.github.domwood.kiwi.kafka.utils;

import com.github.domwood.kiwi.data.input.ConsumerStartPosition;
import org.apache.kafka.common.TopicPartition;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

import static com.github.domwood.kiwi.utilities.StreamUtils.mapCollector;

public class KafkaOffsetPositionCalculator {

    private KafkaOffsetPositionCalculator() {
    }

    public static Map<TopicPartition, Long> getStartingPositions(Map<TopicPartition, Long> startOffsets,
                                                                 Map<TopicPartition, Long> endOffsets,
                                                                 ConsumerStartPosition startPosition) {
        if (isPositionDefinedThroughOffsets(startPosition, endOffsets)) {
            return startingPositionsFromOffsets(startPosition, endOffsets);
        }
        else {
            Map<TopicPartition, Double> percentagePerPartition;
            if(isPositionDefinedThroughPercentages(startPosition, endOffsets)){
                percentagePerPartition = asPercentagePerPartition(startPosition, endOffsets);
            }
            else{
                percentagePerPartition = asPercentagePerPartitionFromTopicPercentage(startPosition, endOffsets);
            }
            return startingPositionsFromPercentage(startOffsets, endOffsets, percentagePerPartition);
        }
    }

    private static Map<TopicPartition, Long> startingPositionsFromOffsets(ConsumerStartPosition startPostition, Map<TopicPartition, Long> endOffsets) {
        return startPostition.offsets().entrySet().stream()
                .map(kv -> asEntry(fromPartitionNumber(kv.getKey(), endOffsets), kv.getValue()))
                .map(kv -> asEntry(kv, rangeCheckOffsetValue(endOffsets.get(kv.getKey()), kv)))
                .collect(mapCollector());
    }

    private static Map<TopicPartition, Long> startingPositionsFromPercentage(Map<TopicPartition, Long> startOffsets,
                                                                             Map<TopicPartition, Long> endOffsets,
                                                                             Map<TopicPartition, Double> startPositions) {
        return endOffsets.entrySet().stream()
                .map(kv -> asEntry(kv, calculateDiff(startOffsets.get(kv.getKey()), kv.getValue())))
                .map(kv -> asEntry(kv, calculateOffset(startOffsets.get(kv.getKey()), kv.getValue(), startPositions.get(kv.getKey()))))
                .map(kv -> asEntry(kv, rangeCheckOffsetValue(endOffsets.get(kv.getKey()), kv)))
                .collect(mapCollector());
    }

    private static long calculateDiff(Long startOffset, Long endOffset){
        return endOffset - startOffset;
    }

    private static long calculateOffset(Long startOffset, Long offsetDifference, Double percentage){
        return ((long) (offsetDifference.doubleValue() * percentage)) + startOffset;
    }

    private static long rangeCheckOffsetValue(Long endOffset, Map.Entry<TopicPartition, Long> entry) {
        if(endOffset >= 0 && endOffset <= entry.getValue()){
            return endOffset;
        }
        else if(entry.getValue() < 0){
            return 0L;
        }
        return entry.getValue();
    }

    private static <K, V, V2> Map.Entry<K, V2> asEntry(Map.Entry<K, V> oldEntry, V2 newValue) {
        return asEntry(oldEntry.getKey(), newValue);
    }

    private static <K, V2> Map.Entry<K, V2> asEntry(K partition, V2 newValue) {
        return new AbstractMap.SimpleEntry<>(partition, newValue);
    }

    private static Map<TopicPartition, Double> asPercentagePerPartitionFromTopicPercentage(ConsumerStartPosition startPostition, Map<TopicPartition, Long> endOffsets) {
        Double percentage = Optional.ofNullable(startPostition.topicPercentage()).orElse(0.0);
        return endOffsets.keySet().stream()
                .map(key -> asEntry(key, percentage))
                .collect(mapCollector());
    }

    private static Map<TopicPartition, Double> asPercentagePerPartition(ConsumerStartPosition startPostition, Map<TopicPartition, Long> endOffsets){
        return startPostition.percentages().entrySet().stream()
                .map(kv -> asEntry(fromPartitionNumber(kv.getKey(), endOffsets), kv.getValue()))
                .collect(mapCollector());
    }

    private static TopicPartition fromPartitionNumber(Integer partition, Map<TopicPartition, Long> endOffsets) {
        return endOffsets.keySet().stream()
                .filter(tp -> tp.partition() == partition)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Requested partition that did not exist"));
    }

    private static boolean isPositionDefinedThroughPercentages(ConsumerStartPosition position, Map<TopicPartition, Long> endOffsets){
        return position.percentages().size() == endOffsets.size();
    }

    private static boolean isPositionDefinedThroughOffsets(ConsumerStartPosition position, Map<TopicPartition, Long> endOffsets){
        return position.offsets().size() == endOffsets.size();
    }
}
