package com.github.domwood.kiwi.utilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static java.util.stream.Collectors.toList;

/**
 * Selection of helper methods which remove some boiler plate
 * java8 stream code, eg extracting a list of fields from a list of objects
 */
public class StreamUtils {

    /**
     *
     * @param input Input list to extract from
     * @param refiner Function to use to extract
     * @param <I> Input type
     * @param <O> Output type
     * @return A list of output type, derived from a field of the input type
     */
    public static <I, O> List<O> extract(Collection<I> input, Function<I, O> refiner){
        return input.stream().map(refiner).collect(toList());
    }

    /**
     *
     * @param input Input map to extract from
     * @param refiner Function which takes a map entry and returns a single value
     * @param <I_K> Input Key type
     * @param <I_V> Input value type
     * @param <O> Output type
     * @return A List of objects of type O from derived from the input map
     */
    public static <I_K, I_V, O> List<O> extract(Map<I_K, I_V> input, Function<Map.Entry<I_K, I_V>, O> refiner){
        return input.entrySet().stream().map(refiner).collect(toList());
    }

    /**
     *
     * @param input Input map to extract from
     * @param refiner Function which take 2 params a key & value from a map
     * @param <I_K> Input Key type
     * @param <I_V>  Input value type
     * @param <O> Output type
     * @return A List of objects of type O from derived from the input map
     */
    public static <I_K, I_V, O> List<O> extract(Map<I_K, I_V> input, BiFunction<I_K, I_V, O> refiner){
        return input.entrySet().stream().map((kv) -> refiner.apply(kv.getKey(), kv.getValue())).collect(toList());
    }

    /**
     *
     * @param list List of Elements
     * @param extractor Element to int function
     * @param <I> Type of element
     * @return Largest int returned via extractor function or 0 if no max returned (eg for an empty list)
     */
    public static <I> Integer maximum(List<I> list, ToIntFunction<I> extractor){
        return list.stream().mapToInt(extractor).max().orElse(0);
    }

    /**
     * Arbitrary map deduplicator
     *
     * @param o1 Input object 1
     * @param o2 Input object 2
     * @param <T> Type of input
     * @return returns object1
     */
    public static <T> T arbitrary(T o1, T o2){
        return o1;
    }

}
