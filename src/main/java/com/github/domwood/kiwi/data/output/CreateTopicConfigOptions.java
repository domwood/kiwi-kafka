package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@JsonSerialize(as = ImmutableCreateTopicConfigOptions.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface CreateTopicConfigOptions {
    Set<String> configOptions();
}
