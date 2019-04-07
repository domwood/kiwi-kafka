package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@JsonSerialize(as = ImmutableBrokerInfoList.class)
@Value.Immutable
public interface BrokerInfoList {

    List<BrokerInfo> brokerInfo();

}
