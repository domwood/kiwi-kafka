package com.github.domwood.kiwi.kafka.utils;

import org.apache.kafka.common.header.Header;

import java.nio.charset.Charset;
import java.util.Map;

public class KafkaHeader implements Header {

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
        return value != null ? value.getBytes(Charset.forName("UTF-8")) : new byte[0];
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public byte[] value() {
        return this.value;
    }
}
