package com.github.domwood.kiwi.kafka.utils;

import com.github.domwood.kiwi.data.input.ConsumerStartPostition;
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
                                                                 ConsumerStartPostition startPostition) {
        if (startPostition.offsets().size() == startOffsets.size()) {
            return startingPositionsFromOffsets(startPostition, endOffsets);
        } else {
            return startingPositionsFromPercentage(startOffsets, endOffsets, asPercentagePerPartition(startPostition, endOffsets));
        }
    }

    private static Map<TopicPartition, Long> startingPositionsFromOffsets(ConsumerStartPostition startPostition, Map<TopicPartition, Long> endOffsets) {
        return startPostition.offsets().entrySet().stream()
                .map(kv -> asEntry(fromPartitionNumber(kv.getKey(), endOffsets), kv.getValue()))
                .collect(mapCollector());
    }

    private static Map<TopicPartition, Long> startingPositionsFromPercentage(Map<TopicPartition, Long> startOffsets,
                                                                             Map<TopicPartition, Long> endOffsets,
                                                                             Map<TopicPartition, Double> startPositions) {
        return endOffsets.entrySet().stream()
                .map(kv -> asEntry(kv, startOffsets.get(kv.getKey()) - kv.getValue()))
                .map(kv -> asEntry(kv, (long) (kv.getValue().doubleValue() * startPositions.get(kv.getKey()))))
                .map(kv -> asEntry(kv, getOffsetValue(endOffsets, kv)))
                .collect(mapCollector());
    }

    private static <K, V, V2> Map.Entry<K, V2> asEntry(Map.Entry<K, V> oldEntry, V2 newValue) {
        return asEntry(oldEntry.getKey(), newValue);
    }

    private static <K, V2> Map.Entry<K, V2> asEntry(K partition, V2 newValue) {
        return new AbstractMap.SimpleEntry<>(partition, newValue);
    }

    private static long getOffsetValue(Map<TopicPartition, Long> endOffsets, Map.Entry<TopicPartition, Long> entry) {
        return endOffsets.get(entry.getKey()) - 1 == entry.getValue() ?
                Math.max(0, entry.getValue() - 1L) : entry.getValue();
    }

    private static Map<TopicPartition, Double> asPercentagePerPartition(ConsumerStartPostition startPostition, Map<TopicPartition, Long> endOffsets) {
        if(startPostition.percentages().size() == endOffsets.size()){
            return startPostition.percentages().entrySet().stream()
                    .map(kv -> asEntry(fromPartitionNumber(kv.getKey(), endOffsets), kv.getValue()))
                    .collect(mapCollector());
        }
        else{
            Double percentage = Optional.ofNullable(startPostition.topicPercentage()).orElse(0.0);
            return endOffsets.keySet().stream()
                    .map(key -> asEntry(key, percentage))
                    .collect(mapCollector());
        }
    }

    private static TopicPartition fromPartitionNumber(Integer partition, Map<TopicPartition, Long> endOffsets) {
        return endOffsets.keySet().stream()
                .filter(tp -> tp.partition() == partition)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Requested partition that did not exist"));
    }


}
