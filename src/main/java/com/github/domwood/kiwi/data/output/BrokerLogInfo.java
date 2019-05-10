package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(as = ImmutableBrokerLogInfo.class)
@JsonSerialize(as = ImmutableBrokerLogInfo.class)
@Value.Immutable
public interface BrokerLogInfo extends OutboundResponse{

    String logName();

    String errorType();

    List<BrokerLogTopicInfo> topicInfoList();

}
