package com.github.domwood.kiwi.data.input;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableMessageAcknowledge.class)
@JsonDeserialize(as = ImmutableMessageAcknowledge.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface MessageAcknowledge extends InboundRequest{
}
