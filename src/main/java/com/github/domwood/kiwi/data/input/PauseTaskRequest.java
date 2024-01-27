package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutablePauseTaskRequest.class)
@JsonDeserialize(as = ImmutablePauseTaskRequest.class)
@Value.Immutable
public interface PauseTaskRequest extends InboundRequest {
    @Value.Default
    default boolean pauseSession() {
        return false;
    }
}
