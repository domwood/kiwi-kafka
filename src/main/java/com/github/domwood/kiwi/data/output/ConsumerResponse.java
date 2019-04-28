package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(as = ImmutableConsumerResponse.class)
@JsonSerialize(as = ImmutableConsumerResponse.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerResponse<K, V> {
    List<ConsumedMessage<K, V>> messages();
}
