package com.github.domwood.kiwi.data.output;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableConsumerGroupOffset.class)
@JsonSerialize(as = ImmutableConsumerGroupOffset.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerGroupOffset extends OutboundResponse{
    String topic();
    String groupId();
    Integer partition();
    Long partitionOffset();
    Long groupOffset();
    Long lag();
    String groupState();
    String coordinator();
}
