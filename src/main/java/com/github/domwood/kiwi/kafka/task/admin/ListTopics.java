package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.api.output.ImmutableTopicList;
import com.github.domwood.kiwi.api.output.TopicList;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.stream.Collectors.toList;

public class ListTopics implements KafkaTask<Void, TopicList, KafkaAdminResource> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CompletableFuture<TopicList> execute(KafkaAdminResource resource, Void input) {
        try{
            AdminClient adminClient = resource.provisionResource();
            ListTopicsResult topicsResult = adminClient.listTopics();

            return toCompletable(topicsResult.listings())
                    .thenApply(result -> ImmutableTopicList.builder()
                            .addAllTopics(result.stream().map(TopicListing::name).collect(toList()))
                            .build());
        }
        catch (Exception e){
            logger.error("Failed to execute list topics task", e);
            resource.discard();
            return failedFuture(e);
        }
    }

}
