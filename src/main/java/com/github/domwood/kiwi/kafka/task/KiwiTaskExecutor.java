package com.github.domwood.kiwi.kafka.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class KiwiTaskExecutor implements Executor {
    private final ThreadPoolExecutor delegate;

    private KiwiTaskExecutor(final ThreadFactory kiwiFactory) {
        delegate = new ThreadPoolExecutor(50, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                kiwiFactory);
    }

    private static class KiwiTaskExecutorHelper {
        private static final KiwiTaskExecutor INSTANCE = new KiwiTaskExecutor(new ThreadFactoryBuilder()
                .setNameFormat("kiwi-task-thread-%d")
                .build());
    }

    public static KiwiTaskExecutor getInstance() {
        return KiwiTaskExecutorHelper.INSTANCE;
    }

    @Override
    public void execute(Runnable command) {
        delegate.submit(command);
    }
}
