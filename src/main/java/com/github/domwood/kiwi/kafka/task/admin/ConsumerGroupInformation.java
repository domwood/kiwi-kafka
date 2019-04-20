package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ConsumerGroupList;
import com.github.domwood.kiwi.data.output.ImmutableConsumerGroupList;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;

public class ConsumerGroupInformation implements KafkaTask<Void, ConsumerGroupList, KafkaAdminResource> {
    @Override
    public CompletableFuture<ConsumerGroupList> execute(KafkaAdminResource resource, Void input) {
        AdminClient adminClient = resource.provisionResource();
        return toCompletable(adminClient.listConsumerGroups().all())
                .thenApply(this::consumerGroupList);
    }

    private ConsumerGroupList consumerGroupList(Collection<ConsumerGroupListing> listing){
        return ImmutableConsumerGroupList.builder()
                .groups(listing.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toSet()))
                .build();
    }

}
