package com.github.domwood.kiwi.kafka.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class KiwiTaskExecutor implements Executor {
    private static final int CORE_POOL_SIZE = 50;
    private final ThreadPoolExecutor delegate;

    private KiwiTaskExecutor(final ThreadFactory kiwiFactory) {
        delegate = new ThreadPoolExecutor(CORE_POOL_SIZE, Integer.MAX_VALUE,
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
    public void execute(final Runnable command) {
        delegate.submit(command);
    }

    public String executorInformation() {
        return String.format("Workers %s/%s (Active/Total)",
                delegate.getActiveCount(), delegate.getPoolSize());
    }
}
