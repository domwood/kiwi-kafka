package com.github.domwood.kiwi.data.output;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@JsonDeserialize(as = ImmutableTopicGroupAssignmentWithOffset.class)
@JsonSerialize(as = ImmutableTopicGroupAssignmentWithOffset.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface TopicGroupAssignmentWithOffset {
    String topic();
    Integer partition();
    @Nullable
    String clientId();
    @Nullable String consumerId();
    String groupId();
    String groupState();
    String coordinator();
    PartitionOffset offset();
}
