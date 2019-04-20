package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@JsonSerialize(as = ImmutableConsumerGroupTopicDetails.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerGroupTopicDetails {
    Map<String, Map<String, List<TopicGroupAssignment>>> topicDetails();
}
