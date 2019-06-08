package com.github.domwood.kiwi.data.input.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableMessageFilter.class)
@JsonSerialize(as = ImmutableMessageFilter.class)
@Value.Immutable
public interface MessageFilter {
    FilterType filterType();
    FilterApplication filterApplication();
    Boolean isCaseSensitive();
    String filter();

}
