package com.github.domwood.kiwi.kafka.task;

import java.io.Closeable;
import java.util.Optional;
import java.util.function.Consumer;

public interface KafkaContinuousTask<I, O> extends Closeable {

    public void pause(Optional<Integer> pauseAfterMatchCount);

    public void unpause(Optional<Integer> pauseAfterMatchCount);

    public void update(I input);

    public void registerConsumer(Consumer<O> consumer);

    public boolean isClosed();

}
