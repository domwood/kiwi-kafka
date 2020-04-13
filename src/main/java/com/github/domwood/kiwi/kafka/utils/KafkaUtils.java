package com.github.domwood.kiwi.kafka.utils;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class KafkaUtils {

    private KafkaUtils() {
    }

    public static Iterable<Header> toKafkaHeaders(Map<String, String> headers) {
        return headers.entrySet()
                .stream()
                .map(KafkaHeader::new)
                .collect(toList());
    }


    public static Map<String, Object> fromKafkaHeaders(Headers headers) {
        return Optional.ofNullable(headers)
                .map(Headers::toArray)
                .map(Arrays::asList)
                .orElse(emptyList())
                .stream()
                .collect(toMap(Header::key, h -> KafkaHeader.valueAsString(h.value())));
    }

}
