package com.github.domwood.kiwi.data.input;

import com.github.domwood.kiwi.data.input.filter.MessageFilter;

import java.util.List;

public interface AbstractConsumerRequest extends InboundRequest {

    List<String> topics();
    Integer limit();
    Boolean limitAppliesFromStart();
    List<MessageFilter> filters();
}
