package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@JsonSerialize(as = ImmutableConsumerStartPosition.class)
@JsonDeserialize(as = ImmutableConsumerStartPosition.class)
@Value.Immutable
public interface ConsumerStartPosition {

    default Set<Integer> partitions() {
        return Collections.emptySet();
    }

    @Value.Default
    default Double topicPercentage() {
        return 0.0;
    }

    @Value.Default
    default Map<Integer, Long> offsets() {
        return Collections.emptyMap();
    }

    @Value.Default
    default Map<Integer, Double> percentages() {
        return Collections.emptyMap();
    }
}
