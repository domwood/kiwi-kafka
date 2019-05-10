package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.data.output.OutboundResponse;

import java.util.function.Consumer;

public interface KafkaContinuousTask<I, R> extends KafkaTask<I, Void, R> {

    public void close();

    public void pause();

    public void update(I input);

    public void registerConsumer(Consumer<OutboundResponse> consumer);

    public boolean isClosed();

}
