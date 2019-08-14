package com.github.domwood.kiwi.data.input;

import java.util.Map;

public interface ConsumerStartPostition {
    Double topicPercentage();
    Map<Integer, Long> offsets();
    Map<Integer, Double> percentages();
}
