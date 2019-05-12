package com.github.domwood.kiwi.kafka.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class KiwiTaskExecutor implements Executor {
    private final ExecutorService delegate;

    private KiwiTaskExecutor(ThreadFactory kiwiFactory){
        delegate = Executors.newFixedThreadPool(100, kiwiFactory);
    }

    private static class KiwiTaskExecutorHelper{
        private final static KiwiTaskExecutor INSTANCE = new KiwiTaskExecutor(new ThreadFactoryBuilder()
                .setNameFormat("kiwi-task-thread-%d")
                .build());
    }

    public static KiwiTaskExecutor getInstance(){
        return KiwiTaskExecutorHelper.INSTANCE;
    }

    @Override
    public void execute(Runnable command) {
        delegate.submit(command);
    }
}
