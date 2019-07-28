package com.github.domwood.kiwi.data.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonDeserialize(as = ImmutableConsumedMessageLine.class)
@JsonSerialize(as = ImmutableConsumedMessageLine.class)
@Value.Immutable
public interface ConsumedMessageLine {

    @JsonInclude(NON_NULL)
    @Nullable
    @JsonProperty("Key")
    String key();

    @JsonInclude(NON_NULL)
    @Nullable
    @JsonProperty("Timestamp")
    Long timestamp();

    @JsonInclude(NON_NULL)
    @Nullable
    @JsonProperty("Partition")
    Integer partition();

    @JsonInclude(NON_NULL)
    @Nullable
    @JsonProperty("Offset")
    Long offset();

    @JsonInclude(NON_NULL)
    @Nullable
    @JsonProperty("Headers")
    Map<String, Object> headers();

    @JsonInclude(NON_NULL)
    @Nullable
    @JsonProperty("Value")
    String value();
}
