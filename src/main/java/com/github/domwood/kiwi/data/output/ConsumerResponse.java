package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Map;

@JsonSerialize(as = ImmutableConsumerResponse.class)
@Value.Immutable
public interface ConsumerResponse<K, V> {

    K message();

    V key();

    Map<String, Object> headers();

}
