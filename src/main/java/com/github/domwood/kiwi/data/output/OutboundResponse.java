package com.github.domwood.kiwi.data.output;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, property="responseType")
public interface OutboundResponse {
}
