package com.github.domwood.kiwi.kafka.filters;

import com.github.domwood.kiwi.data.input.filter.FilterApplication;
import com.github.domwood.kiwi.data.input.filter.FilterType;
import com.github.domwood.kiwi.data.input.filter.ImmutableMessageFilter;
import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.kafka.utils.KafkaHeader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilterBuilderTest {

    @Mock
    ConsumerRecord<String, String> mockRecord;

    @Mock
    Headers headers;

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,true",
            "WORLD HELLO,false",
            "$NULL,false",
            "hello world,true"
    }, nullValues = "$NULL")
    public void testBuildStartsWithFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter().build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,false",
            "WORLD HELLO,true",
            "$NULL,false",
            "world hello,true"
    }, nullValues = "$NULL")
    public void testBuildEndsWithFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.ENDS_WITH)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,true",
            "WORLD HELLO,false",
            "$NULL,false",
            "hello world,false"
    }, nullValues = "$NULL")
    public void testBuildCaseInsensitiveFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .isCaseSensitive(true)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,true",
            "WORLD HELLO,true",
            "$NULL,false",
            "hello world,false",
            "helo world,false"
    }, nullValues = "$NULL")
    public void testBuildRegexFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.REGEX)
                .filter("HE[L]{2}[Oo]")
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,true",
            "WORLD HELLO,true",
            "$NULL,false"
    }, nullValues = "$NULL")
    public void testBuildContainsFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.CONTAINS)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder

                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,false",
            "WELCOME YOU,true",
            "HELLO YOU,false",
            "hello YOU,true",
            "$NULL,true"
    }, nullValues = "$NULL")
    public void testBuildDoesNotContainCaseSensitiveFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.NOT_CONTAINS)
                .isCaseSensitive(true)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "hello world,false",
            "welcome world,true",
            "hello you,false",
            "HELLO you,false",
            "$NULL,true"
    }, nullValues = "$NULL")
    public void testBuildDoesNotContainFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.NOT_CONTAINS)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,false",
            "WORLD HELLO,false",
            "$NULL,false",
            "HELLO,true"
    }, nullValues = "$NULL")
    public void testBuildMatchesFilter(final String keyValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.MATCHES)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key()).thenReturn(keyValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HELLO WORLD,true",
            "WORLD HELLO,false",
            "$NULL,false",
            "hello world,true"
    }, nullValues = "$NULL")
    public void testValueFilterApplication(final String recordValue, final boolean isMatching) {

        MessageFilter filter = baseFilter()
                .filterApplication(FilterApplication.VALUE)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.value()).thenReturn(recordValue);

        assertEquals(isMatching, test.test(mockRecord));
    }

    @Test
    public void testHeaderKeyFilterApplication() {
        when(mockRecord.headers()).thenReturn(headers);

        MessageFilter filter = baseFilter()
                .filterApplication(FilterApplication.HEADER_KEY)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(headers.toArray())
                .thenReturn(new Header[]{createHeader("HELLO", "WORLD")})
                .thenReturn(new Header[]{createHeader("WORLD", "HELLO")})
                .thenReturn(new Header[]{createHeader(null, null)});

        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
    }

    @Test
    public void testHeaderValueFilterApplication() {
        when(mockRecord.headers()).thenReturn(headers);

        MessageFilter filter = baseFilter()
                .filterApplication(FilterApplication.HEADER_VALUE)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(headers.toArray())
                .thenReturn(new Header[]{createHeader("HELLO", "WORLD")})
                .thenReturn(new Header[]{createHeader("WORLD", "HELLO")})
                .thenReturn(new Header[]{createHeader(null, null)});

        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, GREATER_THAN, 10, false",
            "10, GREATER_THAN, 10, false",
            "10, GREATER_THAN, 0, true",

            "0, LESS_THAN, 10, true",
            "10, LESS_THAN, 10, false",
            "10, LESS_THAN, 0, false",

            "0, MATCHES, 10, false",
            "10, MATCHES, 10, true",
            "10, MATCHES, 0, false",

            "0, NOT_MATCHES, 10, true",
            "10, NOT_MATCHES, 10, false",
            "10, NOT_MATCHES, 0, true"
    })
    public void testOffsetFilterApplication(final long offsetValue,
                                            final FilterType filterType,
                                            final long filterValue,
                                            final boolean matches) {
        when(mockRecord.offset()).thenReturn(offsetValue);

        MessageFilter filter = baseFilter()
                .filterApplication(FilterApplication.OFFSET)
                .filterType(filterType)
                .filter(Long.toString(filterValue))
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        assertEquals(matches, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, GREATER_THAN, 10, false",
            "10, GREATER_THAN, 10, false",
            "10, GREATER_THAN, 0, true",

            "0, LESS_THAN, 10, true",
            "10, LESS_THAN, 10, false",
            "10, LESS_THAN, 0, false",

            "0, MATCHES, 10, false",
            "10, MATCHES, 10, true",
            "10, MATCHES, 0, false",

            "0, NOT_MATCHES, 10, true",
            "10, NOT_MATCHES, 10, false",
            "10, NOT_MATCHES, 0, true"
    })
    public void testTimestampFilterApplication(final long offsetValue,
                                               final FilterType filterType,
                                               final long filterValue,
                                               final boolean matches) {
        when(mockRecord.timestamp()).thenReturn(offsetValue);

        MessageFilter filter = baseFilter()
                .filterApplication(FilterApplication.TIMESTAMP)
                .filterType(filterType)
                .filter(Long.toString(filterValue))
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        assertEquals(matches, test.test(mockRecord));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, GREATER_THAN, 10, false",
            "10, GREATER_THAN, 10, false",
            "10, GREATER_THAN, 0, true",

            "0, LESS_THAN, 10, true",
            "10, LESS_THAN, 10, false",
            "10, LESS_THAN, 0, false",

            "0, MATCHES, 10, false",
            "10, MATCHES, 10, true",
            "10, MATCHES, 0, false",

            "0, NOT_MATCHES, 10, true",
            "10, NOT_MATCHES, 10, false",
            "10, NOT_MATCHES, 0, true"
    })
    public void testPartitionFilterApplication(final long offsetValue,
                                               final FilterType filterType,
                                               final long filterValue,
                                               final boolean matches) {
        when(mockRecord.partition()).thenReturn(Long.valueOf(offsetValue).intValue());

        MessageFilter filter = baseFilter()
                .filterApplication(FilterApplication.PARTITION)
                .filterType(filterType)
                .filter(Long.toString(filterValue))
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        assertEquals(matches, test.test(mockRecord));
    }

    private ImmutableMessageFilter.Builder baseFilter() {
        return ImmutableMessageFilter.builder()
                .filter("HELLO")
                .filterApplication(FilterApplication.KEY)
                .filterType(FilterType.STARTS_WITH)
                .isCaseSensitive(false);
    }

    private Header createHeader(String key, String value) {
        return new KafkaHeader(key, value);
    }

}
