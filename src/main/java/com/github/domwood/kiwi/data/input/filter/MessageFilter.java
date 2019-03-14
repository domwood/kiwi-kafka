package com.github.domwood.kiwi.data.input.filter;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface MessageFilter {
    FilterType filterType();
    FilterApplication filterApplication();
    Boolean isCaseSensitive();
    String filter();
    Optional<String> headerKey();

}
