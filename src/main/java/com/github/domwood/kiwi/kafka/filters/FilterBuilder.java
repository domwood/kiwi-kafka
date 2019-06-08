package com.github.domwood.kiwi.kafka.filters;

import com.github.domwood.kiwi.data.input.filter.MessageFilter;
import com.github.domwood.kiwi.kafka.utils.KafkaUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FilterBuilder {

    private FilterBuilder() {}

    public static Predicate<ConsumerRecord<String, String>> compileFilters(List<MessageFilter> messageFilterList){
        return messageFilterList.stream().map(FilterBuilder::compileFilter)
                .reduce(Predicate::and)
                .orElse(dummyFilter());
    }

    private static Predicate<ConsumerRecord<String, String>> compileFilter(MessageFilter messageFilter){
        switch(messageFilter.filterApplication()){
            case KEY:
                return record -> buildFilterType(messageFilter).test(keyExtractor(record));
            case VALUE:
                return record -> buildFilterType(messageFilter).test(valueExtractor(record));
            case HEADER_KEY:
                return record -> headerKeyExtractor(record, buildFilterType(messageFilter));
            case HEADER_VALUE:
                return record -> headerValueExtractor(record, buildFilterType(messageFilter));
            default:
                return dummyFilter();
        }
    }

    private static Predicate<String> buildFilterType(MessageFilter messageFilter){
        switch(messageFilter.filterType()){
            case STARTS_WITH:
                return messageFilter.isCaseSensitive() ?
                        startsWith(messageFilter.filter()) : startsWithCaseInsensitive(messageFilter.filter());
            case ENDS_WITH:
                return messageFilter.isCaseSensitive() ?
                        endsWith(messageFilter.filter()) : endsWithCaseInsensitive(messageFilter.filter());
            case CONTAINS:
                return messageFilter.isCaseSensitive() ?
                        contains(messageFilter.filter()) : containsCaseInsensitive(messageFilter.filter());
            case MATCHES:
                return messageFilter.isCaseSensitive() ?
                        matches(messageFilter.filter()) : matchesCaseInsensitive(messageFilter.filter());
            case REGEX:
                return regex(messageFilter.filter());
            default:
                return s -> true;
        }
    }

    private static  String keyExtractor(ConsumerRecord<String, String> record){
        return record.key();
    }

    private static  String valueExtractor(ConsumerRecord<String, String> record){
        return record.value();
    }

    private static  Boolean headerKeyExtractor(ConsumerRecord<String, String> record, Predicate<String> headerMatcher){
        return KafkaUtils.fromKafkaHeaders(record.headers())
                .keySet()
                .stream()
                .anyMatch(headerMatcher);
    }

    private static  Boolean headerValueExtractor(ConsumerRecord<String, String> record, Predicate<String> headerMatcher){
        return KafkaUtils.fromKafkaHeaders(record.headers())
                .values()
                .stream()
                .map(String::valueOf)
                .anyMatch(headerMatcher);
    }

    private static Predicate<String> startsWithCaseInsensitive(String filterString){
        return (String value) -> value != null && filterString != null && value.toLowerCase().startsWith(filterString.toLowerCase());
    }

    private static Predicate<String> startsWith(String filterString){
        return (String value) -> value != null && filterString != null && value.startsWith(filterString);
    }

    private static Predicate<String> endsWithCaseInsensitive(String filterString){
        return (String value) -> value != null && filterString != null && value.toLowerCase().endsWith(filterString.toLowerCase());
    }

    private static Predicate<String> endsWith(String filterString){
        return (String value) -> value != null && filterString != null && value.endsWith(filterString);
    }

    private static Predicate<String> containsCaseInsensitive(String filterString){
        return (String value) -> value != null && filterString != null && value.toLowerCase().contains(filterString.toLowerCase());
    }

    private static Predicate<String> contains(String filterString){
        return (String value) -> value != null && filterString != null && value.contains(filterString);
    }

    private static Predicate<String> matchesCaseInsensitive(String filterString){
        return (String value) -> value != null && filterString != null && value.equalsIgnoreCase(filterString);
    }

    private static Predicate<String> matches(String filterString){
        return (String value) -> value != null && filterString != null && value.equals(filterString);
    }

    private static Predicate<String> regex(String filterString){
        Pattern p = Pattern.compile(filterString);
        return (String value) -> p.matcher(value).find();
    }

    private static Predicate<ConsumerRecord<String, String>> dummyFilter(){
        return record -> true;
    }
}
