package com.github.domwood.kiwi.kafka.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class KafkaUtils {

    private KafkaUtils() {
    }

    public static Iterable<Header> toKafkaHeaders(List<Pair<String, String>> headers) {
        return headers.stream()
                .filter(header -> Objects.nonNull(header.getKey()))
                .map(KafkaHeader::new)
                .collect(toList());
    }


    public static List<Pair<String, String>> fromKafkaHeaders(Headers headers) {
        return Optional.ofNullable(headers)
                .map(Headers::toArray)
                .map(Arrays::asList)
                .orElse(emptyList())
                .stream()
                .map(header -> Pair.of(header.key(), KafkaHeader.valueAsString(header.value())))
                .collect(toList());
    }

}
