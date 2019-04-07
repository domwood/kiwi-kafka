package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@JsonSerialize(as = ImmutableBrokerLogInfo.class)
@Value.Immutable
public interface BrokerLogInfo {

    String logName();

    String errorType();

    List<BrokerLogTopicInfo> topicInfoList();

}
