package com.github.domwood.kiwi.data.input;

import org.immutables.value.Value;

import java.util.Collections;
import java.util.Map;

@Value.Immutable
public interface ConsumerStartPosition {

    @Value.Default
    default Double topicPercentage(){
        return 0.0;
    }

    @Value.Default
    default Map<Integer, Long> offsets(){
        return Collections.emptyMap();
    }

    @Value.Default
    default Map<Integer, Double> percentages(){
        return Collections.emptyMap();
    }
}
