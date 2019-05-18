package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.Arrays.asList;

public class DeleteConsumerGroup implements KafkaTask<String, Void, KafkaAdminResource> {
    @Override
    public CompletableFuture<Void> execute(KafkaAdminResource resource, String input) {
        return toCompletable(resource.deleteConsumerGroups(asList(input)).all());
    }
}
