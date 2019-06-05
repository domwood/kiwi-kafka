package com.github.domwood.kiwi.kafka.task;

import com.github.domwood.kiwi.kafka.resources.AbstractKafkaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractKafkaTask<I, O, R extends AbstractKafkaResource> implements KafkaTask<O> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final R resource;
    protected final I input;

    public AbstractKafkaTask(R resource, I input){
        this.resource = resource;
        this.input = input;
    }

    public CompletableFuture<O> execute() {
        CompletableFuture<O> output = this.delegateExecute();
        output.whenComplete(this::handleCompletion);
        return output;
    }

    protected abstract CompletableFuture<O> delegateExecute();

    private void handleCompletion(O outcome, Throwable e){
        if(e != null){
            logger.error("Task completed with failure ", e);
        }
        else{
            logger.info("Task completed without error");
        }
        this.resource.discard();
    }

    protected R getResource() {
        return resource;
    }

    protected I getInput() {
        return input;
    }
}
