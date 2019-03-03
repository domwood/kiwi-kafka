package com.github.domwood.kiwi.api.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@JsonDeserialize(as = ImmutableProducerRequest.class)
@Value.Immutable
public interface ProducerRequest {
    String topic();
    String key();
    Map<String, String> headers();
    Optional<String> payload();
}
