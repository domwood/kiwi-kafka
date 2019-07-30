package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.kafka.resources.AbstractKafkaResource;
import com.github.domwood.kiwi.utilities.FutureUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Provides an interface for the task to return a O return type.
 * This class then wraps the return into a CompletableFuture<O>
 * to provide the parent class's return type.
 *
 * @param <I>
 * @param <O>
 * @param <R>
 */
public abstract class FuturisingAbstractKafkaTask<I, O, R extends AbstractKafkaResource> extends AbstractKafkaTask<I, O, R> {

    public FuturisingAbstractKafkaTask(R resource, I input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<O> delegateExecute() {
        return FutureUtils.supplyAsync(this::delegateExecuteSync);
    }

    protected abstract O delegateExecuteSync();
}
