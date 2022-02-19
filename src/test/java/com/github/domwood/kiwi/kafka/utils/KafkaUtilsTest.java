package com.github.domwood.kiwi.kafka.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaUtilsTest {

    @Test
    public void convertEmptyHeaders() {
        final Headers headers = new RecordHeaders();
        final List<Pair<String, String>> observed = KafkaUtils.fromKafkaHeaders(headers);
        assertEquals(emptyList(), observed);
    }

    @Test
    public void convertHeadersWithStrings() {
        final Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("test", "test".getBytes(StandardCharsets.UTF_8)));
        final List<Pair<String, String>> observed = KafkaUtils.fromKafkaHeaders(headers);
        assertEquals(singletonList(Pair.of("test", "test")), observed);
    }

    @Test
    public void convertEmptyHeaderValues() {
        final Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("test", new byte[0]));
        final List<Pair<String, String>> observed = KafkaUtils.fromKafkaHeaders(headers);
        assertEquals(singletonList(Pair.of("test", "")), observed);
    }

    @Test
    public void convertHeaderWhichArentStringsToSomething() {
        final Headers headers = new RecordHeaders();
        final byte[] data = new byte[4];
        data[0] = 0x1;
        data[1] = 0x1;
        data[2] = 0x2;
        data[3] = 0xF;
        headers.add(new RecordHeader("test", data));
        final List<Pair<String, String>> observed = KafkaUtils.fromKafkaHeaders(headers);
        assertEquals(singletonList(Pair.of("test", "\u0001\u0001\u0002\u000F")), observed);
    }

    @Test
    public void convertHeadersWithDuplicateKeys() {
        final Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("test", "test1".getBytes(StandardCharsets.UTF_8)));
        headers.add(new RecordHeader("test", "test2".getBytes(StandardCharsets.UTF_8)));
        final List<Pair<String, String>> observed = KafkaUtils.fromKafkaHeaders(headers);
        final List<Pair<String, String>> expected = asList(Pair.of("test", "test1"), Pair.of("test", "test2"));
        assertEquals(expected, observed);
    }

    @Test
    public void convertStringKafkaHeaders() {
        final List<Pair<String, String>> input = singletonList(Pair.of("test", "test1"));
        Iterable<Header> observed = KafkaUtils.toKafkaHeaders(input);
        List<KafkaHeader> expected = singletonList(new KafkaHeader("test", "test1"));

        assertEquals(expected, observed);
    }

    @Test
    public void convertDuplicateKafkaHeaders() {
        final List<Pair<String, String>> input = asList(Pair.of("test", "test1"), Pair.of("test", "test2"));
        Iterable<Header> observed = KafkaUtils.toKafkaHeaders(input);
        List<KafkaHeader> expected = asList(new KafkaHeader("test", "test1"), new KafkaHeader("test", "test2"));

        assertEquals(expected, observed);
    }

    @Test
    public void convertHeadersWithNullValues() {
        final List<Pair<String, String>> input = singletonList(Pair.of("test", null));
        Iterable<Header> observed = KafkaUtils.toKafkaHeaders(input);
        List<KafkaHeader> expected = singletonList(new KafkaHeader("test", null));

        assertEquals(expected, observed);
    }

}
