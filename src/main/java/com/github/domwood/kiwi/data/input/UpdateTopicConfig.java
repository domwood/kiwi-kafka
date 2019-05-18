package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Map;

@JsonSerialize(as = ImmutableUpdateTopicConfig.class)
@JsonDeserialize(as = ImmutableUpdateTopicConfig.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface UpdateTopicConfig extends InboundRequest {
    String topic();
    Map<String, String> config();

}
