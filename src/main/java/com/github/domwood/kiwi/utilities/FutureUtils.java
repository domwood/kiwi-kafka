package com.github.domwood.kiwi.utilities;

import com.github.domwood.kiwi.exceptions.KiwiFutureException;
import com.github.domwood.kiwi.kafka.task.KiwiTaskExecutor;

import java.sql.Time;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class FutureUtils {
    private FutureUtils(){}

    public static <T> CompletableFuture<T> toCompletable(Future<T> future, long timeout, TimeUnit unit){
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get(timeout, unit);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new KiwiFutureException(e);
            }
        }, KiwiTaskExecutor.getInstance());
    }

    public static <T> CompletableFuture<T> toCompletable(Future<T> future){
        return toCompletable(future, 10, TimeUnit.MINUTES);
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable ex){
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier){
        return CompletableFuture.supplyAsync(supplier, KiwiTaskExecutor.getInstance());
    }

}
