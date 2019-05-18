package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ConsumerGroups;
import com.github.domwood.kiwi.data.output.ImmutableConsumerGroups;
import com.github.domwood.kiwi.data.output.ImmutableTopicGroupAssignment;
import com.github.domwood.kiwi.data.output.TopicGroupAssignment;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.utilities.StreamUtils;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.common.TopicPartition;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.kafka.task.KafkaTaskUtils.formatCoordinator;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.stream.Collectors.toList;

public class AllConsumerGroupDetails implements KafkaTask<Void, ConsumerGroups, KafkaAdminResource> {
    @Override
    public CompletableFuture<ConsumerGroups> execute(KafkaAdminResource resource, Void input) {

        return toCompletable(resource.listConsumerGroups().all())
                .thenCompose(list -> fromConsumerGroupList(resource, list));
    }

    private CompletableFuture<ConsumerGroups> fromConsumerGroupList(KafkaAdminResource resource, Collection<ConsumerGroupListing> groupListings){
        DescribeConsumerGroupsResult result = resource.describeConsumerGroups(groupListings.stream()
                .map(ConsumerGroupListing::groupId).collect(toList()));
        return toCompletable(result.all())
                .thenApply(resolved -> ImmutableConsumerGroups.builder()
                        .topicDetails(asTopicAssignments(resolved))
                        .build());
    }

    private Map<String, Map<String, List<TopicGroupAssignment>>> asTopicAssignments(Map<String, ConsumerGroupDescription> details){
        return details.entrySet().stream()
                .flatMap(entry -> asTopicAssignments(entry).stream())
                .collect(Collectors.groupingBy(TopicGroupAssignment::topic))
                .entrySet()
                .stream()
                .map(kv -> new AbstractMap.SimpleEntry<>(kv.getKey(), kv.getValue().stream()
                        .collect(Collectors.groupingBy(TopicGroupAssignment::groupId))))
                .collect(StreamUtils.mapCollector());
    }

    private List<TopicGroupAssignment> asTopicAssignments(Map.Entry<String, ConsumerGroupDescription> description){
        return description.getValue()
                .members().stream()
                .flatMap(member -> assignment(member, description.getValue()).stream())
                .collect(toList());
    }

    private List<TopicGroupAssignment> assignment(MemberDescription memberDescription,
                                                  ConsumerGroupDescription consumerDescription){
        return memberDescription.assignment().topicPartitions().stream()
                .map(tp -> topicGroupAssignment(tp, memberDescription, consumerDescription))
                .collect(toList());

    }

    private TopicGroupAssignment topicGroupAssignment(TopicPartition topicPartition,
                                                      MemberDescription memberDescription,
                                                      ConsumerGroupDescription consumerGroupDescription){
        return ImmutableTopicGroupAssignment.builder()
                .topic(topicPartition.topic())
                .partition(topicPartition.partition())
                .clientId(memberDescription.clientId())
                .groupId(memberDescription.consumerId())
                .groupId(consumerGroupDescription.groupId())
                .groupState(consumerGroupDescription.state().name())
                .coordinator(formatCoordinator(consumerGroupDescription.coordinator()))
                .build();
    }

}
