package com.github.domwood.kiwi.kafka.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class KiwiTaskExecutor implements Executor {
    private static KiwiTaskExecutor instance;
    private final ExecutorService delegate;

    private KiwiTaskExecutor(ThreadFactory kiwiFactory){
        delegate = Executors.newFixedThreadPool(100, kiwiFactory);
    }

    static{
        try{
            instance = new KiwiTaskExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("kiwi-runtime-thread-%d")
                    .build());
        }
        catch(Exception e){
            throw new RuntimeException("Exception occured in creating singleton instance");
        }
    }

    public static KiwiTaskExecutor getInstance(){
        return instance;
    }

    @Override
    public void execute(Runnable command) {
        delegate.submit(command);
    }
}
