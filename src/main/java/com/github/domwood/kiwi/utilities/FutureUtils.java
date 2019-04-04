package com.github.domwood.kiwi.utilities;

import com.github.domwood.kiwi.kafka.task.KiwiTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class FutureUtils {

    public static <T> CompletableFuture<T> toCompletable(Future<T> future){
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, KiwiTaskExecutor.getInstance());
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
