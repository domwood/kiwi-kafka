package com.github.domwood.kiwi.kafka.utils;

import org.apache.kafka.common.header.Header;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class KafkaHeader implements Header {

    private static final Charset format = StandardCharsets.UTF_8;

    private final String key;
    private final byte[] value;

    public KafkaHeader(Map.Entry<String, String> mapEntry) {
        this.key = mapEntry.getKey();
        this.value = asBytes(mapEntry.getValue());
    }

    public KafkaHeader(String key, String value) {
        this.key = key;
        this.value = asBytes(value);
    }

    private byte[] asBytes(String value){
        return value != null ? value.getBytes(format) : new byte[0];
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public byte[] value() {
        return this.value;
    }

    public static String valueAsString(byte[] value) {
        return new String(value, format);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaHeader that = (KafkaHeader) o;
        return Objects.equals(key, that.key) && Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}
