package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@JsonDeserialize(as = ImmutableConsumerGroupOffsetDetails.class)
@JsonSerialize(as = ImmutableConsumerGroupOffsetDetails.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerGroupOffsetDetails extends OutboundResponse{
    Map<String, List<ConsumerGroupOffset>> offsets();
}
