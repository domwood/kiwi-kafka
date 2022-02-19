package com.github.domwood.kiwi.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;

public class CustomHeaderMapSerializer extends JsonSerializer<CustomHeaderMapSerializer.CustomerHeaderMap> {

    @Override
    public void serialize(final CustomerHeaderMap csvHeaderMap,
                          final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        for (Pair<String, String> header : csvHeaderMap.headers) {
            jsonGenerator.writeStringField(header.getKey(), header.getValue());
        }
        jsonGenerator.writeEndObject();
    }

    public static CustomerHeaderMap toSerializable(final List<Pair<String, String>> headers) {
        return new CustomerHeaderMap(headers);
    }

    public static class CustomerHeaderMap {
        private final List<Pair<String, String>> headers;

        private CustomerHeaderMap(List<Pair<String, String>> headers) {
            this.headers = headers;
        }
    }

}
