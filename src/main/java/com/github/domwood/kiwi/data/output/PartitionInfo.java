package com.github.domwood.kiwi.data.output;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(as = ImmutablePartitionInfo.class)
@JsonSerialize(as = ImmutablePartitionInfo.class)
@Value.Immutable
public interface PartitionInfo extends OutboundResponse{

    Integer partition();
    Integer replicationfactor();
    List<Integer> replicas();
    List<Integer> isrs();
    Integer leader();

}
