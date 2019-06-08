package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.Arrays.asList;

public class DeleteTopic extends AbstractKafkaTask<String, Void, KafkaAdminResource> {

    public DeleteTopic(KafkaAdminResource resource, String input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<Void> delegateExecute() {
        return toCompletable(resource.deleteTopics(asList(input)).all());
    }
}
