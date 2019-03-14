package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableProducerResponse.class)
@Value.Immutable
public interface ProducerResponse {
    String topic();
    Integer partition();
    Long offset();
}
