package com.github.domwood.kiwi.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class PairSerializer extends JsonSerializer<Pair> {

    @Override
    public void serialize(final Pair pair,
                          final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("key", pair.getKey());
        jsonGenerator.writeObjectField("value", pair.getValue());
        jsonGenerator.writeEndObject();
    }
}