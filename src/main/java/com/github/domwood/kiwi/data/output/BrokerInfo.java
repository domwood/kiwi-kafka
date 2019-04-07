package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@JsonSerialize(as = ImmutableBrokerInfo.class)
@Value.Immutable
public interface BrokerInfo {

    public Integer nodeNumber();

    public String nodeName();

    public String host();

    public Integer port();

    @Nullable
    public String nodeRack();

}
