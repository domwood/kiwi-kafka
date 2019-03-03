package com.github.domwood.kiwi.kafka.utils;

import org.apache.kafka.common.header.Header;

import java.util.Map;

import static java.util.stream.Collectors.toList;

public class KafkaUtils {

    public static Iterable<Header> toKafkaHeaders(Map<String, String> headers){
        return headers.entrySet()
                .stream()
                .map(KafkaHeader::new)
                .collect(toList());
    }
}
