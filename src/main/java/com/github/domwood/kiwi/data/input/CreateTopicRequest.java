package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Map;

@JsonSerialize(as = ImmutableCreateTopicRequest.class)
@JsonDeserialize(as = ImmutableCreateTopicRequest.class)
@Value.Immutable
public interface CreateTopicRequest extends InboundRequest {
    String name();
    Integer partitions();
    Integer replicationFactor();
    Map<String, String> configuration();
}
