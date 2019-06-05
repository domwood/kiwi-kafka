package com.github.domwood.kiwi.kafka.task;


import java.util.concurrent.CompletableFuture;

public interface KafkaTask<O> {

    public CompletableFuture<O> execute();

}
