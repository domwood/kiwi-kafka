package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@JsonSerialize(as = ImmutableProducerRequest.class)
@JsonDeserialize(as = ImmutableProducerRequest.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ProducerRequest extends InboundRequest {
    String topic();

    String key();

    Map<String, String> headers();

    Optional<String> payload();

    @Value.Default
    default KafkaDataType kafkaKeyDataType() {
        return KafkaDataType.STRING;
    }

    @Value.Default
    default KafkaDataType kafkaValueDataType() {
        return KafkaDataType.STRING;
    }
}
