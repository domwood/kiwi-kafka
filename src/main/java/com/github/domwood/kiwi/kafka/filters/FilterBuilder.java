package com.github.domwood.kiwi.kafka.filters;

import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.kafka.utils.KafkaUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FilterBuilder {

    private FilterBuilder() {
    }

    public static <K, V> Predicate<ConsumerRecord<K, V>> compileFilters(List<MessageFilter> messageFilterList,
                                                                        Function<K, String> keyHandler,
                                                                        Function<V, String> valueHandler) {
        return messageFilterList.stream()
                .filter(FilterBuilder::isFilterValid)
                .map(f -> FilterBuilder.compileFilter(f, keyHandler, valueHandler))
                .reduce(Predicate::and)
                .orElse(dummyFilter());
    }

    private static <K, V> Predicate<ConsumerRecord<K, V>> compileFilter(MessageFilter messageFilter,
                                                                        Function<K, String> keyHandler,
                                                                        Function<V, String> valueHandler) {
        switch (messageFilter.filterApplication()) {
            case KEY:
                return record -> buildFilterType(messageFilter).test(keyExtractor(record, keyHandler));
            case VALUE:
                return record -> buildFilterType(messageFilter).test(valueExtractor(record, valueHandler));
            case HEADER_KEY:
                return record -> headerKeyExtractor(record, buildFilterType(messageFilter));
            case HEADER_VALUE:
                return record -> headerValueExtractor(record, buildFilterType(messageFilter));
            default:
                return dummyFilter();
        }
    }

    private static Predicate<String> buildFilterType(MessageFilter messageFilter) {
        switch (messageFilter.filterType()) {
            case STARTS_WITH:
                return messageFilter.isCaseSensitive() ?
                        startsWith(messageFilter.filter()) : startsWithCaseInsensitive(messageFilter.filter());
            case ENDS_WITH:
                return messageFilter.isCaseSensitive() ?
                        endsWith(messageFilter.filter()) : endsWithCaseInsensitive(messageFilter.filter());
            case CONTAINS:
                return messageFilter.isCaseSensitive() ?
                        contains(messageFilter.filter()) : containsCaseInsensitive(messageFilter.filter());
            case NOT_CONTAINS:
                return messageFilter.isCaseSensitive() ?
                        doesNotContain(messageFilter.filter()) : doesNotContainCaseInsensitive(messageFilter.filter());
            case MATCHES:
                return messageFilter.isCaseSensitive() ?
                        matches(messageFilter.filter()) : matchesCaseInsensitive(messageFilter.filter());
            case REGEX:
                return regex(messageFilter.filter());
            default:
                return s -> true;
        }
    }

    private static <K> String keyExtractor(ConsumerRecord<K, ?> record, Function<K, String> keyHandler) {
        return keyHandler.apply(record.key());
    }

    private static <V> String valueExtractor(ConsumerRecord<?, V> record, Function<V, String> valueHandler) {
        return valueHandler.apply(record.value());
    }

    private static Boolean headerKeyExtractor(ConsumerRecord<?, ?> record, Predicate<String> headerMatcher) {
        return KafkaUtils.fromKafkaHeaders(record.headers())
                .keySet()
                .stream()
                .anyMatch(headerMatcher);
    }

    private static Boolean headerValueExtractor(ConsumerRecord<?, ?> record, Predicate<String> headerMatcher) {
        return KafkaUtils.fromKafkaHeaders(record.headers())
                .values()
                .stream()
                .map(String::valueOf)
                .anyMatch(headerMatcher);
    }

    private static Predicate<String> startsWithCaseInsensitive(String filterString) {
        return (String value) -> value != null && value.toLowerCase().startsWith(filterString.toLowerCase());
    }

    private static Predicate<String> startsWith(String filterString) {
        return (String value) -> value != null && value.startsWith(filterString);
    }

    private static Predicate<String> endsWithCaseInsensitive(String filterString) {
        return (String value) -> value != null && value.toLowerCase().endsWith(filterString.toLowerCase());
    }

    private static Predicate<String> endsWith(String filterString) {
        return (String value) -> value != null && value.endsWith(filterString);
    }

    private static Predicate<String> containsCaseInsensitive(String filterString) {
        return (String value) -> value != null && value.toLowerCase().contains(filterString.toLowerCase());
    }

    private static Predicate<String> contains(String filterString) {
        return (String value) -> value != null && value.contains(filterString);
    }

    private static Predicate<String> doesNotContainCaseInsensitive(String filterString) {
        return (String value) -> value == null || !value.toLowerCase().contains(filterString.toLowerCase());
    }

    private static Predicate<String> doesNotContain(String filterString) {
        return (String value) -> value == null || !value.contains(filterString);
    }

    private static Predicate<String> matchesCaseInsensitive(String filterString){
        return (String value) -> value != null && value.equalsIgnoreCase(filterString);
    }

    private static Predicate<String> matches(String filterString) {
        return (String value) -> value != null && value.equals(filterString);
    }

    private static Predicate<String> regex(String filterString) {
        Pattern p = Pattern.compile(filterString);
        return (String value) -> value != null && p.matcher(value).find();
    }

    private static <K, V> Predicate<ConsumerRecord<K, V>> dummyFilter() {
        return unused -> true;
    }

    private static boolean isFilterValid(MessageFilter messageFilter) {
        return Objects.nonNull(messageFilter.filter());
    }
}
