package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@JsonDeserialize(as = ImmutableCreateTopicConfigOptions.class)
@JsonSerialize(as = ImmutableCreateTopicConfigOptions.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface CreateTopicConfigOptions extends OutboundResponse {
    Set<String> configOptions();
}
