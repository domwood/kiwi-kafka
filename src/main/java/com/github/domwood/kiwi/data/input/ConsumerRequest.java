package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import org.immutables.value.Value;

import java.util.List;

@JsonSerialize(as = ImmutableConsumerRequest.class)
@JsonDeserialize(as = ImmutableConsumerRequest.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerRequest extends AbstractConsumerRequest {

}
