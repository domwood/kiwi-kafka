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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilterBuilderTest {

    @Mock
    ConsumerRecord<String, String> mockRecord;

    @Mock
    Headers headers;

    @Test
    public void testBuildStartsWithFilter() {

        MessageFilter filter = baseFilter().build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("HELLO WORLD")
                .thenReturn("WORLD HELLO")
                .thenReturn(null)
                .thenReturn("hello world");

        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
    }

    @Test
    public void testBuildEndsWithFilter() {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.ENDS_WITH)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("HELLO WORLD")
                .thenReturn("WORLD HELLO")
                .thenReturn(null)
                .thenReturn("world hello");

        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
    }

    @Test
    public void testBuildCaseInsensitiveFilter() {

        MessageFilter filter = baseFilter()
                .isCaseSensitive(true)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("HELLO WORLD")
                .thenReturn("WORLD HELLO")
                .thenReturn(null)
                .thenReturn("hello world");

        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
    }

    @Test
    public void testBuildRegexFilter() {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.REGEX)
                .filter("HE[L]{2}[Oo]")
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("HELLO WORLD")
                .thenReturn("WORLD HELLO")
                .thenReturn(null)
                .thenReturn("hello world")
                .thenReturn("helo world")
        ;

        assertTrue(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
    }

    @Test
    public void testBuildContainsFilter() {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.CONTAINS)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder

                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("HELLO WORLD")
                .thenReturn("WORLD HELLO")
                .thenReturn(null)
        ;

        assertTrue(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
    }

    @Test
    public void testBuildDoesNotContainCaseSensitiveFilter() {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.NOT_CONTAINS)
                .isCaseSensitive(true)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("HELLO WORLD")
                .thenReturn("WELCOME YOU")
                .thenReturn("HELLO YOU")
                .thenReturn("hello YOU")
                .thenReturn(null);

        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
    }

    @Test
    public void testBuildDoesNotContainFilter() {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.NOT_CONTAINS)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("hello world")
                .thenReturn("welcome world")
                .thenReturn("hello you")
                .thenReturn("HELLO you")
                .thenReturn(null);

        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
    }

    @Test
    public void testBuildMatchesFilter() {

        MessageFilter filter = baseFilter()
                .filterType(FilterType.MATCHES)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.key())
                .thenReturn("HELLO WORLD")
                .thenReturn("WORLD HELLO")
                .thenReturn(null)
                .thenReturn("HELLO")
        ;

        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
    }

    @Test
    public void testValueFilterApplication() {

        MessageFilter filter = baseFilter()
                .filterApplication(FilterApplication.VALUE)
                .build();

        Predicate<ConsumerRecord<String, String>> test = FilterBuilder
                .compileFilters(singletonList(filter), Function.identity(), Function.identity());

        when(mockRecord.value())
                .thenReturn("HELLO WORLD")
                .thenReturn("WORLD HELLO")
                .thenReturn(null)
                .thenReturn("hello world");

        assertTrue(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertFalse(test.test(mockRecord));
        assertTrue(test.test(mockRecord));
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
