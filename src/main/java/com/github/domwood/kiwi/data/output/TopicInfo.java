package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@JsonSerialize(as = ImmutableTopicInfo.class)
@Value.Immutable
public interface TopicInfo {
    String topic();
    Integer partitionCount();
    Integer replicaCount();
    List<PartitionInfo> partitions();
    Map<String, String> configuration();
}
