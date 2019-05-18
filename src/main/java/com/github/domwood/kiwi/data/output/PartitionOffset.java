package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutablePartitionOffset.class)
@JsonSerialize(as = ImmutablePartitionOffset.class)
@Value.Immutable
public interface PartitionOffset extends OutboundResponse {
    Long partitionOffset();
    Long groupOffset();
    Long lag();
}
