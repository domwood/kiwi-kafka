package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@JsonDeserialize(as = ImmutableConsumerGroupTopicDetails.class)
@JsonSerialize(as = ImmutableConsumerGroupTopicDetails.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerGroupTopicDetails extends OutboundResponse{
    Map<String, Map<String, List<TopicGroupAssignment>>> topicDetails();
}
