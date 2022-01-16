package com.github.domwood.kiwi.kafka.resources;

import java.util.function.Function;

public class KafkaDataTypeHandler<T> {

    private final Function<T, String> toStringConverter;
    private final Function<String, T> toTypeConverter;
    private final String kafkaSerializer;
    private final String kafkaDeserializer;

    public KafkaDataTypeHandler(
            Function<T, String> toStringConverter,
            Function<String, T> toTypeConverter,
            String kafkaSerializer,
            String kafkaDeserializer
    ) {
        this.toStringConverter = toStringConverter;
        this.toTypeConverter = toTypeConverter;
        this.kafkaSerializer = kafkaSerializer;
        this.kafkaDeserializer = kafkaDeserializer;
    }

    public String convert(T type) {
        return this.toStringConverter.apply(type);
    }

    public T convert(String input) {
        return this.toTypeConverter.apply(input);
    }

    public String getKafkaSerializer() {
        return kafkaSerializer;
    }

    public String getKafkaDeserializer() {
        return kafkaDeserializer;
    }
}
