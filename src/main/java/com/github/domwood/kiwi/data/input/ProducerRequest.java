package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.tuple.Pair;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@JsonSerialize(as = ImmutableProducerRequest.class)
@JsonDeserialize(as = ImmutableProducerRequest.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ProducerRequest extends InboundRequest {
    String topic();

    String key();

    List<Pair<String, String>> headers();

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
