package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@JsonDeserialize(as = ImmutableConsumerRequest.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerRequest {
    List<String> topics();
    Integer limit();
    Boolean limitAppliesFromStart();
    Optional<MessageFilter> filter();
}
