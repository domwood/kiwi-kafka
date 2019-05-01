package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ConsumerGroupTopicDetails;
import com.github.domwood.kiwi.data.output.ImmutableConsumerGroupTopicDetails;
import com.github.domwood.kiwi.data.output.ImmutableTopicGroupAssignment;
import com.github.domwood.kiwi.data.output.TopicGroupAssignment;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.utilities.StreamUtils;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.MemberDescription;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

//Task pivots data from consumer group to topics
public class ConsumerGroupTopicInformation implements KafkaTask<Void, ConsumerGroupTopicDetails, KafkaAdminResource> {
    @Override
    public CompletableFuture<ConsumerGroupTopicDetails> execute(KafkaAdminResource resource, Void input) {

        return toCompletable(resource.listConsumerGroups().all())
                .thenCompose(list -> fromConsumerGroupList(resource, list));
    }

    private CompletableFuture<ConsumerGroupTopicDetails> fromConsumerGroupList(KafkaAdminResource resource, Collection<ConsumerGroupListing> groupListings){
        DescribeConsumerGroupsResult result = resource.describeConsumerGroups(groupListings.stream()
                .map(ConsumerGroupListing::groupId).collect(toList()));
        return toCompletable(result.all())
                .thenApply(resolved -> ImmutableConsumerGroupTopicDetails.builder()
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
        return description.getValue().members().stream()
                .flatMap(member -> assignment(description.getKey(), member).stream())
                .collect(toList());
    }

    private List<TopicGroupAssignment> assignment(String groupId, MemberDescription memberDescription){
        return memberDescription.assignment().topicPartitions().stream()
                .map(tp -> ImmutableTopicGroupAssignment.builder()
                            .topic(tp.topic())
                            .partition(tp.partition())
                            .clientId(memberDescription.clientId())
                            .groupId(memberDescription.consumerId())
                            .groupId(groupId)
                            .build())
                .collect(toList());

    }


}
