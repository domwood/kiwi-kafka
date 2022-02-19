package com.github.domwood.kiwi.data.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Objects;

public class PairDeserializer extends JsonDeserializer<Pair<String, String>> {
    @Override
    public Pair<String, String> deserialize(final JsonParser jsonParser,
                                            final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String key = null;
        String value = null;
        while (jsonParser.currentToken() != JsonToken.END_OBJECT) {
            JsonToken keyToken = jsonParser.nextToken();
            if (keyToken == JsonToken.FIELD_NAME && Objects.equals("key", jsonParser.currentName())) {
                JsonToken valueToken = jsonParser.nextToken();
                if (valueToken == JsonToken.VALUE_STRING) {
                    key = jsonParser.getText();
                }
            }
            if (keyToken == JsonToken.FIELD_NAME && Objects.equals("value", jsonParser.currentName())) {
                JsonToken valueToken = jsonParser.nextToken();
                if (valueToken == JsonToken.VALUE_STRING) {
                    value = jsonParser.getText();
                }
            }
        }
        return Pair.of(key, value);
    }
}
