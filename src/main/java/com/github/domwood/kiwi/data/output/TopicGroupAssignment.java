package com.github.domwood.kiwi.data.output;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@JsonDeserialize(as = ImmutableTopicGroupAssignment.class)
@JsonSerialize(as = ImmutableTopicGroupAssignment.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface TopicGroupAssignment {

    String topic();
    Integer partition();
    @Nullable String clientId();
    @Nullable String consumerId();
    String groupId();

}
