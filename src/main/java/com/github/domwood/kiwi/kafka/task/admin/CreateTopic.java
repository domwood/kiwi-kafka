package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.input.CreateTopicRequest;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.Arrays.asList;

public class CreateTopic extends AbstractKafkaTask<CreateTopicRequest, Void, KafkaAdminResource> {
    public CreateTopic(KafkaAdminResource resource, CreateTopicRequest input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<Void> delegateExecute() {
        NewTopic newTopic = asNewTopic(input);
        return toCompletable(resource.createTopics(asList(newTopic))
                .values()
                .get(input.name()));
    }

    private NewTopic asNewTopic(CreateTopicRequest input){
        return new NewTopic(input.name(), input.partitions(),input.replicationFactor().shortValue())
                .configs(input.configuration());
    }

}
