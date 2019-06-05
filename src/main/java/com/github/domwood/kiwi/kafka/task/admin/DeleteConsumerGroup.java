package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.Arrays.asList;

public class DeleteConsumerGroup extends AbstractKafkaTask<String, Void, KafkaAdminResource> {

    public DeleteConsumerGroup(KafkaAdminResource resource, String input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<Void> delegateExecute() {
        return toCompletable(resource.deleteConsumerGroups(asList(input)).all());
    }
}
