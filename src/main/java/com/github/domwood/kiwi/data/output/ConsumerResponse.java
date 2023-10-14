package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@JsonDeserialize(as = ImmutableConsumerResponse.class)
@JsonSerialize(as = ImmutableConsumerResponse.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerResponse extends OutboundResponseWithPosition {
    Optional<ConsumerPosition> position();

    List<ConsumedMessage> messages();

    @Value.Default
    default boolean paused() {
        return false;
    }
}
