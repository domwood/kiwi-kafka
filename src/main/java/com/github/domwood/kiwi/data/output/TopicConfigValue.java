package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableTopicConfigValue.class)
@JsonDeserialize(as = ImmutableTopicConfigValue.class)
public interface TopicConfigValue {
    String configKey();
    String configValue();
    @Nullable String configDescription();
    Boolean isDefault();
}
