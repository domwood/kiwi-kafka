package com.github.domwood.kiwi.data.input.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(as = ImmutableMessageFilter.class)
@Value.Immutable
public interface MessageFilter {
    FilterType filterType();
    FilterApplication filterApplication();
    Boolean isCaseSensitive();
    String filter();
    Optional<String> headerKey();

}
