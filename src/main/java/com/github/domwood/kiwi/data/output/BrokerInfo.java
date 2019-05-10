package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@JsonDeserialize(as = ImmutableBrokerInfo.class)
@JsonSerialize(as = ImmutableBrokerInfo.class)
@Value.Immutable
public interface BrokerInfo extends OutboundResponse{

    public Integer nodeNumber();

    public String nodeName();

    public String host();

    public Integer port();

    @Nullable
    public String nodeRack();

}
