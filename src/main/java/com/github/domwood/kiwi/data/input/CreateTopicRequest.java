package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Map;

@JsonDeserialize(as = ImmutableCreateTopicRequest.class)
@Value.Immutable
public interface CreateTopicRequest {
    String name();
    Integer partitions();
    Integer replicationFactor();
    Map<String, String> configuration();
}
