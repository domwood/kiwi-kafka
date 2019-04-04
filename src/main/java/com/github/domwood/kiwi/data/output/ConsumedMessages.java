package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Map;

@JsonSerialize(as = ImmutableConsumedMessages.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumedMessages<K, V> {

    long timestamp();

    int partition();

    long offset();

    @Nullable
    K message();

    @Nullable
    V key();

    Map<String, Object> headers();

}
