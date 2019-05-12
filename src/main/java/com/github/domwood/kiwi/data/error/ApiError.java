package com.github.domwood.kiwi.data.error;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@JsonSerialize(as = ImmutableApiError.class)
@JsonDeserialize(as = ImmutableApiError.class)
@Value.Immutable
public interface ApiError {
    @Nullable String message();
    String error();
    String rootCause();
    String stackTrace();
}
