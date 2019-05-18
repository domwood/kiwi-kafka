package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.kafka.task.KafkaTaskUtils;
import com.github.domwood.kiwi.utilities.FutureUtils;
import com.github.domwood.kiwi.utilities.StreamUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.kafka.task.KafkaTaskUtils.formatCoordinator;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;


public class ConsumerGroupDetailsWithOffset implements KafkaTask<String, ConsumerGroupTopicWithOffsetDetails, Pair<KafkaAdminResource, KafkaConsumerResource<?, ?>>> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public CompletableFuture<ConsumerGroupTopicWithOffsetDetails> execute(Pair<KafkaAdminResource, KafkaConsumerResource<?, ?>> resource,
                                                                          String groupId) {
        CompletableFuture<Map<TopicPartition, OffsetAndMetadata>> groupAssignment =
                toCompletable(resource.getLeft().listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata());

        CompletableFuture<Map<TopicPartition, Pair<OffsetAndMetadata, Long>>> groupAssignmentAndOffset = groupAssignment
                .thenCompose(assignment -> withOffsets(resource.getRight(), assignment));

        CompletableFuture<ConsumerGroupDescription> description =
                toCompletable(resource.getLeft().describeConsumerGroups(asList(groupId))
                        .describedGroups()
                        .get(groupId));

        return groupAssignmentAndOffset
                .thenCombine(description, this::toOffsetDetails)
                .whenComplete((group, error) -> {
                    if(error != null){
                        logger.error("Task completed with error", error);
                    }
                    resource.getRight().discard();
                });
    }

    private ConsumerGroupTopicWithOffsetDetails toOffsetDetails(Map<TopicPartition, Pair<OffsetAndMetadata, Long>> partitionOffsetData,
                                                                ConsumerGroupDescription description) {
        Map<String, List<TopicGroupAssignmentWithOffset>> data = asByTopicAndByPartition(partitionOffsetData, description);
        return ImmutableConsumerGroupTopicWithOffsetDetails.builder()
                .offsets(data)
                .build();
    }

    private Map<String, List<TopicGroupAssignmentWithOffset>> asByTopicAndByPartition(Map<TopicPartition, Pair<OffsetAndMetadata, Long>> partitionOffsetData,
                                                                                                    ConsumerGroupDescription description) {
        return StreamUtils.extract(partitionOffsetData, this::asOffset)
                .stream()
                .map(offsetData -> asTopicGroupWithOffset(offsetData, description))
                .collect(Collectors.groupingBy(TopicGroupAssignmentWithOffset::topic));
    }

    private TopicGroupAssignmentWithOffset asTopicGroupWithOffset(Pair<TopicPartition, PartitionOffset> offset,
                                                                  ConsumerGroupDescription description){
        return ImmutableTopicGroupAssignmentWithOffset.builder()
                .groupId(description.groupId())
                .consumerId(getConsumerId(description, offset.getLeft()))
                .clientId(getClientId(description, offset.getLeft()))
                .coordinator(formatCoordinator(description.coordinator()))
                .groupState(description.state().name())
                .topic(offset.getLeft().topic())
                .partition(offset.getLeft().partition())
                .offset(offset.getRight())
                .build();
    }

    private Pair<TopicPartition, PartitionOffset> asOffset(TopicPartition topicPartition, Pair<OffsetAndMetadata, Long> offsetAndMetadata) {
        return Pair.of(topicPartition, ImmutablePartitionOffset.builder()
                .groupOffset(offsetAndMetadata.getKey().offset())
                .partitionOffset(offsetAndMetadata.getRight())
                .lag(offsetAndMetadata.getRight() - offsetAndMetadata.getLeft().offset())
                .build());
    }

    //Appears to be how the ConsumerGroupCommand.scala finds the offset/lag
    private CompletableFuture<Map<TopicPartition, Pair<OffsetAndMetadata, Long>>> withOffsets(KafkaConsumerResource<?, ?> resource, Map<TopicPartition, OffsetAndMetadata> offsetData) {
        return FutureUtils.supplyAsync(() -> {
            List<String> topics = offsetData.keySet().stream()
                    .map(TopicPartition::topic)
                    .distinct()
                    .collect(toList());

            if (topics.isEmpty()) return Collections.emptyMap();

            Map<TopicPartition, Long> endOffsets = KafkaTaskUtils.subscribeAndSeek(resource, topics, false);

            return mapToOffset(offsetData, endOffsets);
        });
    }

    private Map<TopicPartition, Pair<OffsetAndMetadata, Long>> mapToOffset(Map<TopicPartition, OffsetAndMetadata> group, Map<TopicPartition, Long> offsets) {
        return group.entrySet().stream()
                .map(groupEntry -> new AbstractMap.SimpleEntry<>(groupEntry.getKey(), Pair.of(groupEntry.getValue(), offsets.getOrDefault(groupEntry.getKey(), 0L))))
                .collect(StreamUtils.mapCollector());
    }

    private String getConsumerId(ConsumerGroupDescription description, TopicPartition topicPartition){
        return getFromConsumerDescription(description, topicPartition, MemberDescription::consumerId);
    }

    private String getClientId(ConsumerGroupDescription description, TopicPartition topicPartition){
        return getFromConsumerDescription(description, topicPartition, MemberDescription::clientId);
    }

    private String getFromConsumerDescription(ConsumerGroupDescription description,
                                              TopicPartition topicPartition,
                                              Function<MemberDescription, String> extractor){
        return description.members().stream()
                .filter(m -> m.assignment().topicPartitions().contains(topicPartition))
                .map(extractor)
                .findFirst()
                .orElse(null);
    }
}
