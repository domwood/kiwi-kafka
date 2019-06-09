package com.github.domwood.kiwi.data.input;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableCloseTaskRequest.class)
@JsonDeserialize(as = ImmutableCloseTaskRequest.class)
@Value.Immutable
public interface CloseTaskRequest extends InboundRequest{
    @Value.Default
    default Boolean closeSession(){
        return false;
    }
}
