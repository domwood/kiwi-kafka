package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableBrokerLogTopicInfo.class)
@Value.Immutable
public interface BrokerLogTopicInfo {
    Integer partition();
    String topic();
    Long size();
    Long lag();
    Boolean isFuture();
}
