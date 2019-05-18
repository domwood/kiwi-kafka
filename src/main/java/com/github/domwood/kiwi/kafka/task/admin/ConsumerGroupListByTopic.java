package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ConsumerGroupList;
import com.github.domwood.kiwi.data.output.ImmutableConsumerGroupList;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.utilities.FutureUtils;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public class ConsumerGroupListByTopic implements KafkaTask<String, ConsumerGroupList, KafkaAdminResource> {

    @Override
    public CompletableFuture<ConsumerGroupList> execute(KafkaAdminResource resource, String topic) {
        return FutureUtils.toCompletable(resource.listConsumerGroups().all())
                .thenCompose(data -> consumerGroupList(topic, resource, data));
    }

    private CompletableFuture<ConsumerGroupList> consumerGroupList(String topic, KafkaAdminResource resource, Collection<ConsumerGroupListing> listing){
        return FutureUtils.toCompletable(resource.describeConsumerGroups(listing
                .stream()
                .map(ConsumerGroupListing::groupId)
                .collect(toList()))
                .all())
                .thenApply(data -> groupsWithTopic(topic, data));
    }

    private ConsumerGroupList groupsWithTopic(String topic, Map<String, ConsumerGroupDescription> groupDescriptions) {
        return ImmutableConsumerGroupList.builder()
                .groups(groupDescriptions.entrySet().stream()
                        .filter(kv-> groupIdAttachedToTopic(topic, kv.getValue()))
                        .map(Map.Entry::getKey)
                        .collect(toList()))
                .build();
    }

    private boolean groupIdAttachedToTopic(String topic, ConsumerGroupDescription description){
        return getTopicList(description).contains(topic);
    }

    private List<String> getTopicList(ConsumerGroupDescription description){
        return description.members().stream()
                .flatMap(m -> m.assignment().topicPartitions().stream().map(TopicPartition::topic))
                .distinct()
                .collect(toList());
    }
}
