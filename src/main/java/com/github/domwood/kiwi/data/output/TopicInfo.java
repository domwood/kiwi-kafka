package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;
import java.util.SortedMap;

@Value.Immutable
@JsonSerialize(as = ImmutableTopicInfo.class)
@JsonDeserialize(as = ImmutableTopicInfo.class)
public interface TopicInfo extends OutboundResponse{
    String topic();
    Integer partitionCount();
    Integer replicaCount();
    List<PartitionInfo> partitions();
    @Nullable SortedMap<String, TopicConfigValue> configuration();
}
