package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@JsonDeserialize(as = ImmutableConsumerGroups.class)
@JsonSerialize(as = ImmutableConsumerGroups.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerGroups extends OutboundResponse{

    //Map<GroupId, Map<Topic, List<TopicGroupAssignmentWithOffset>>> TODO strongly type
    Map<String, Map<String, List<TopicGroupAssignment>>> topicDetails();
}
