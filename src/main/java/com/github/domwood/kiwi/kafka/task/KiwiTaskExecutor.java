package com.github.domwood.kiwi.kafka.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class KiwiTaskExecutor implements Executor {
    private static final String uniqueTag = UUID.randomUUID().toString().substring(25, 36);
    private static final String prefix = Optional.ofNullable(System.getenv("threadPrefix")).orElse("kiwi-thread");
    private final ExecutorService delegate;

    private KiwiTaskExecutor(ThreadFactory kiwiFactory){
        delegate = Executors.newFixedThreadPool(50, kiwiFactory);
    }

    private static class KiwiTaskExecutorHelper{
        private static final KiwiTaskExecutor INSTANCE = new KiwiTaskExecutor(new ThreadFactoryBuilder()
                .setNameFormat(prefix+"-%d-"+uniqueTag)
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
