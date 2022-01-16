package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Map;

@JsonDeserialize(as = ImmutableConsumedMessage.class)
@JsonSerialize(as = ImmutableConsumedMessage.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumedMessage{

    @Nullable
    String message();

    @Nullable
    String key();

    long timestamp();

    int partition();

    long offset();

    Map<String, String> headers();

}
