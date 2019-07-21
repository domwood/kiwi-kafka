package com.github.domwood.kiwi.kafka.task;

import java.io.Closeable;
import java.util.function.Consumer;

public interface KafkaContinuousTask<I, O> extends Closeable {

    public void pause();

    public void update(I input);

    public void registerConsumer(Consumer<O> consumer);

    public boolean isClosed();

}
