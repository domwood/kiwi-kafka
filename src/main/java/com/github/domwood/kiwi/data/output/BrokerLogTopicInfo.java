package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableBrokerLogTopicInfo.class)
@JsonSerialize(as = ImmutableBrokerLogTopicInfo.class)
@Value.Immutable
public interface BrokerLogTopicInfo extends OutboundResponse{
    Integer partition();
    String topic();
    Long size();
    Long lag();
    Boolean isFuture();
}
