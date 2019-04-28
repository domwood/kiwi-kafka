package com.github.domwood.kiwi.kafka.utils;

import java.util.function.Supplier;

public class TimeProvider {

    private static Supplier<Long> timeProvider = System::currentTimeMillis;

    public static void defineTimeProvider(Supplier<Long> time){
        timeProvider = time;
    }

    public static long getTime(){
        return timeProvider.get();
    }

}
