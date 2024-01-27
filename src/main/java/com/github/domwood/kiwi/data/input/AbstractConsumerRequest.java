package com.github.domwood.kiwi.data.input;

import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

public interface AbstractConsumerRequest extends InboundRequest {

    List<String> topics();

    Integer limit();

    List<MessageFilter> filters();

    Optional<ConsumerStartPosition> consumerStartPosition();

    @Value.Default
    default KafkaDataType kafkaKeyDataType() {
        return KafkaDataType.STRING;
    }

    @Value.Default
    default KafkaDataType kafkaValueDataType() {
        return KafkaDataType.STRING;
    }
}
