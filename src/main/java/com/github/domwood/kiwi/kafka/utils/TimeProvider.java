package com.github.domwood.kiwi.kafka.utils;

import java.util.function.Supplier;

public class TimeProvider {

    private TimeProvider(){}

    private static Supplier<Long> timeFn = System::currentTimeMillis;

    public static void defineTimeProvider(Supplier<Long> time){
        timeFn = time;
    }

    public static long getTime(){
        return timeFn.get();
    }

}
