package com.github.domwood.kiwi.kafka.filters;

import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.kafka.utils.KafkaUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.github.domwood.kiwi.utilities.NumberUtils.safeLong;

public class FilterBuilder {

    private FilterBuilder() {
    }

    public static <K, V> Predicate<ConsumerRecord<K, V>> compileFilters(final List<MessageFilter> messageFilterList,
                                                                        final Function<K, String> keyHandler,
                                                                        final Function<V, String> valueHandler) {
        return messageFilterList.stream()
                .filter(FilterBuilder::isFilterValid)
                .map(f -> FilterBuilder.compileFilter(f, keyHandler, valueHandler))
                .reduce(Predicate::and)
                .orElse(dummyFilter());
    }

    private static <K, V> Predicate<ConsumerRecord<K, V>> compileFilter(final MessageFilter messageFilter,
                                                                        final Function<K, String> keyHandler,
                                                                        final Function<V, String> valueHandler) {
        switch (messageFilter.filterApplication()) {
            case KEY:
                return kafkaRecord -> buildFilterType(messageFilter).test(keyExtractor(kafkaRecord, keyHandler));
            case VALUE:
                return kafkaRecord -> buildFilterType(messageFilter).test(valueExtractor(kafkaRecord, valueHandler));
            case HEADER_KEY:
                return kafkaRecord -> headerKeyExtractor(kafkaRecord, buildFilterType(messageFilter));
            case HEADER_VALUE:
                return kafkaRecord -> headerValueExtractor(kafkaRecord, buildFilterType(messageFilter));
            case PARTITION:
                return kafkaRecord -> partitionExtractor(kafkaRecord, buildNumericFilterType(messageFilter));
            case OFFSET:
                return kafkaRecord -> offsetExtractor(kafkaRecord, buildNumericFilterType(messageFilter));
            case TIMESTAMP:
                return kafkaRecord -> timestampExtractor(kafkaRecord, buildNumericFilterType(messageFilter));
            default:
                return dummyFilter();
        }
    }

    private static Predicate<String> buildFilterType(final MessageFilter messageFilter) {
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
            case NOT_MATCHES:
                return messageFilter.isCaseSensitive() ?
                        notMatches(messageFilter.filter()) : notMatchesCaseInsensitive(messageFilter.filter());
            case REGEX:
                return regex(messageFilter.filter());
            default:
                return s -> true;
        }
    }

    private static Predicate<Long> buildNumericFilterType(final MessageFilter messageFilter) {
        switch (messageFilter.filterType()) {
            case MATCHES:
                return matches(safeLong(messageFilter.filter()));
            case NOT_MATCHES:
                return notMatches(safeLong(messageFilter.filter()));
            case LESS_THAN:
                return lessThan(messageFilter.filter());
            case GREATER_THAN:
                return greaterThan(messageFilter.filter());
            default:
                return s -> true;
        }
    }

    private static <K> String keyExtractor(final ConsumerRecord<K, ?> kafkaRecord, final Function<K, String> keyHandler) {
        return keyHandler.apply(kafkaRecord.key());
    }

    private static <V> String valueExtractor(final ConsumerRecord<?, V> kafkaRecord, final Function<V, String> valueHandler) {
        return valueHandler.apply(kafkaRecord.value());
    }

    private static Boolean headerKeyExtractor(final ConsumerRecord<?, ?> kafkaRecord, final Predicate<String> headerMatcher) {
        return KafkaUtils.fromKafkaHeaders(kafkaRecord.headers())
                .stream()
                .map(Pair::getKey)
                .anyMatch(headerMatcher);
    }

    private static Boolean headerValueExtractor(final ConsumerRecord<?, ?> kafkaRecord, final Predicate<String> headerMatcher) {
        return KafkaUtils.fromKafkaHeaders(kafkaRecord.headers())
                .stream()
                .map(Pair::getValue)
                .map(String::valueOf)
                .anyMatch(headerMatcher);
    }

    private static Boolean partitionExtractor(final ConsumerRecord<?, ?> kafkaRecord, final Predicate<Long> partitionMatcher) {
        return partitionMatcher.test((long) kafkaRecord.partition());
    }

    private static Boolean offsetExtractor(final ConsumerRecord<?, ?> kafkaRecord, final Predicate<Long> offsetMatcher) {
        return offsetMatcher.test(kafkaRecord.offset());
    }

    private static Boolean timestampExtractor(final ConsumerRecord<?, ?> kafkaRecord, final Predicate<Long> timestampMatcher) {
        return timestampMatcher.test(kafkaRecord.timestamp());
    }

    private static Predicate<String> startsWithCaseInsensitive(final String filterString) {
        return (String value) -> value != null && value.toLowerCase().startsWith(filterString.toLowerCase());
    }

    private static Predicate<String> startsWith(final String filterString) {
        return (String value) -> value != null && value.startsWith(filterString);
    }

    private static Predicate<String> endsWithCaseInsensitive(final String filterString) {
        return (String value) -> value != null && value.toLowerCase().endsWith(filterString.toLowerCase());
    }

    private static Predicate<String> endsWith(final String filterString) {
        return (String value) -> value != null && value.endsWith(filterString);
    }

    private static Predicate<String> containsCaseInsensitive(final String filterString) {
        return (String value) -> value != null && value.toLowerCase().contains(filterString.toLowerCase());
    }

    private static Predicate<String> contains(final String filterString) {
        return (String value) -> value != null && value.contains(filterString);
    }

    private static Predicate<String> doesNotContainCaseInsensitive(final String filterString) {
        return (String value) -> value == null || !value.toLowerCase().contains(filterString.toLowerCase());
    }

    private static Predicate<String> doesNotContain(final String filterString) {
        return (String value) -> value == null || !value.contains(filterString);
    }

    private static Predicate<String> matchesCaseInsensitive(final String filterString) {
        return (String value) -> value != null && value.equalsIgnoreCase(filterString);
    }

    private static <T> Predicate<T> matches(final T filterString) {
        return (T value) -> value != null && value.equals(filterString);
    }

    private static <T> Predicate<T> notMatches(final T filterString) {
        return (T value) -> value == null || !value.equals(filterString);
    }

    private static Predicate<String> notMatchesCaseInsensitive(final String filterString) {
        return (String value) -> value == null || !value.equalsIgnoreCase(filterString);
    }

    private static Predicate<String> regex(final String filterString) {
        Pattern p = Pattern.compile(filterString);
        return (String value) -> value != null && p.matcher(value).find();
    }

    private static Predicate<Long> lessThan(final String filterString) {
        return (Long value) -> value != null && value < safeLong(filterString);
    }

    private static Predicate<Long> greaterThan(final String filterString) {
        return (Long value) -> value != null && value > safeLong(filterString);
    }

    private static <K, V> Predicate<ConsumerRecord<K, V>> dummyFilter() {
        return unused -> true;
    }

    private static boolean isFilterValid(final MessageFilter messageFilter) {
        return Objects.nonNull(messageFilter.filter()) && isValidNumericFilter(messageFilter);
    }

    private static boolean isValidNumericFilter(final MessageFilter messageFilter) {
        switch (messageFilter.filterApplication()) {
            case OFFSET:
            case TIMESTAMP:
            case PARTITION:
                switch (messageFilter.filterType()) {
                    case GREATER_THAN:
                    case LESS_THAN:
                    case MATCHES:
                    case NOT_MATCHES:
                        return true;
                    default:
                        return false;
                }
            default:
                switch (messageFilter.filterType()) {
                    case GREATER_THAN:
                    case LESS_THAN:
                        return false;
                    default:
                        return true;
                }
        }
    }

}
