package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@JsonDeserialize(as = ImmutableConsumerGroupTopicWithOffsetDetails.class)
@JsonSerialize(as = ImmutableConsumerGroupTopicWithOffsetDetails.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerGroupTopicWithOffsetDetails extends OutboundResponse{

    Map<String, List<TopicGroupAssignmentWithOffset>> offsets();
}
