package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.domwood.kiwi.data.input.ImmutableProducerRequest;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableProducerResponse.class)
@JsonSerialize(as = ImmutableProducerResponse.class)
@Value.Immutable
public interface ProducerResponse extends OutboundResponse{
    String topic();
    Integer partition();
    Long offset();
}
