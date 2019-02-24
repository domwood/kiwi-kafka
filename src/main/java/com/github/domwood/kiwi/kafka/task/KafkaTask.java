package com.github.domwood.kiwi.kafka.task;


import java.util.concurrent.CompletableFuture;

public interface KafkaTask<I, O, R> {

    public CompletableFuture<O> execute(R resource, I input);

}
