package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(as = ImmutableBrokerInfoList.class)
@JsonSerialize(as = ImmutableBrokerInfoList.class)
@Value.Immutable
public interface BrokerInfoList extends OutboundResponse{

    List<BrokerInfo> brokerInfo();

}
