package com.github.domwood.kiwi.kafka.utils;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class KafkaUtils {

    public static Iterable<Header> toKafkaHeaders(Map<String, String> headers){
        return headers.entrySet()
                .stream()
                .map(KafkaHeader::new)
                .collect(toList());
    }


    public static Map<String, Object> fromKafkaHeaders(Headers headers){
        return asList(headers.toArray()).stream()
                .collect(toMap(header -> header.key(), header -> header.value()));
    }

}
