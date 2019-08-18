package com.github.domwood.kiwi.data.input;

import com.github.domwood.kiwi.data.input.filter.MessageFilter;

import java.util.List;
import java.util.Optional;

public interface AbstractConsumerRequest extends InboundRequest {

    List<String> topics();
    Integer limit();
    List<MessageFilter> filters();
    Optional<ConsumerStartPosition> consumerStartPosition();
}
