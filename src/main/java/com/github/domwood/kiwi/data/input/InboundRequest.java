package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "requestType")
public interface InboundRequest {
    Optional<String> clusterName();
}

