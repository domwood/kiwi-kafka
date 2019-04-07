package com.github.domwood.kiwi.utilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/*
 * Utility method to remove some of the stream boiler plate
 */
public class StreamUtils {

    public static <I, O> List<O> extract(Collection<I> input, Function<I, O> refiner){
        return input.stream().map(refiner).collect(toList());
    }

    public static <I_K, I_V, O> List<O> extract(Map<I_K, I_V> input, Function<Map.Entry<I_K, I_V>, O> refiner){
        return input.entrySet().stream().map(refiner).collect(toList());
    }

    public static <I_K, I_V, O> List<O> extract(Map<I_K, I_V> input, BiFunction<I_K, I_V, O> refiner){
        return input.entrySet().stream().map((kv) -> refiner.apply(kv.getKey(), kv.getValue())).collect(toList());
    }

}
