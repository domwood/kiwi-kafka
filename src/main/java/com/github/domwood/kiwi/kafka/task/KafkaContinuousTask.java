package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.data.output.OutboundResponseWithPosition;

import java.io.Closeable;
import java.util.function.Consumer;

public interface KafkaContinuousTask<I> extends Closeable {

    public void pause();

    public void update(I input);

    public void registerConsumer(Consumer<OutboundResponseWithPosition> consumer);

    public boolean isClosed();

}
