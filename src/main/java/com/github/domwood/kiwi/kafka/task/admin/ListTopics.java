package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ImmutableTopicList;
import com.github.domwood.kiwi.data.output.TopicList;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.stream.Collectors.toList;

public class ListTopics extends AbstractKafkaTask<Void, TopicList, KafkaAdminResource> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ListTopics(KafkaAdminResource resource, Void input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<TopicList> delegateExecute() {
        try{
            ListTopicsResult topicsResult = resource.listTopics();

            return toCompletable(topicsResult.listings())
                    .thenApply(result -> ImmutableTopicList.builder()
                            .addAllTopics(result.stream()
                                    .map(TopicListing::name)
                                    .sorted()
                                    .collect(toList()))
                            .build());
        }
        catch (Exception e){
            logger.error("Failed to execute list topics task", e);
            resource.discard();
            return failedFuture(e);
        }
    }

}
