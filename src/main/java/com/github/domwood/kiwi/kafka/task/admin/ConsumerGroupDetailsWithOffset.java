package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaResourcePair;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.kafka.task.KafkaTaskUtils.formatCoordinator;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.util.Arrays.asList;


public class ConsumerGroupDetailsWithOffset extends AbstractKafkaTask<String, ConsumerGroupTopicWithOffsetDetails, KafkaResourcePair<KafkaAdminResource, KafkaConsumerResource<String, String>>> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ConsumerGroupDetailsWithOffset(KafkaResourcePair<KafkaAdminResource, KafkaConsumerResource<String, String>> resource, String input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<ConsumerGroupTopicWithOffsetDetails> delegateExecute() {
        CompletableFuture<Map<TopicPartition, OffsetAndMetadata>> groupAssignment =
                toCompletable(resource.getLeft().listConsumerGroupOffsets(input).partitionsToOffsetAndMetadata());

        CompletableFuture<ConsumerGroupDescription> description = toCompletable(resource.getLeft().describeConsumerGroups(asList(input))
                .describedGroups()
                .get(input));

        return description
                .thenCombine(groupAssignment, Pair::of)
                .thenCompose(ab -> withOffsets(resource.getRight(), ab.getRight(), ab.getLeft()))
                .thenCombine(description, this::toOffsetDetails)
                .whenComplete((group, error) -> {
                    if (error != null) {
                        logger.error("Task completed with error", error);
                    }
                });
    }

    private ConsumerGroupTopicWithOffsetDetails toOffsetDetails(Map<TopicPartition, Pair<Long, Long>> partitionOffsetData,
                                                                ConsumerGroupDescription description) {
        Map<String, List<TopicGroupAssignmentWithOffset>> data = asByTopicAndByPartition(partitionOffsetData, description);
        return ImmutableConsumerGroupTopicWithOffsetDetails.builder()
                .offsets(data)
                .build();
    }

    private Map<String, List<TopicGroupAssignmentWithOffset>> asByTopicAndByPartition(Map<TopicPartition, Pair<Long, Long>> partitionOffsetData,
                                                                                      ConsumerGroupDescription description) {
        return description.members().stream()
                .flatMap(member -> member.assignment().topicPartitions()
                        .stream()
                        .map(topicPartition -> Pair.of(member, topicPartition)))
                .map(tp -> asTopicGroupWithOffset(asOffset(tp.getRight(), Optional.ofNullable(partitionOffsetData.get(tp.getRight()))), description, tp.getKey()))
                .collect(Collectors.groupingBy(TopicGroupAssignmentWithOffset::topic));
    }

    private TopicGroupAssignmentWithOffset asTopicGroupWithOffset(Pair<TopicPartition, PartitionOffset> offset,
                                                                  ConsumerGroupDescription description,
                                                                  MemberDescription member) {
        return ImmutableTopicGroupAssignmentWithOffset.builder()
                .groupId(description.groupId())
                .consumerId(member.consumerId())
                .clientId(member.clientId())
                .coordinator(formatCoordinator(description.coordinator()))
                .groupState(description.state().name())
                .topic(offset.getLeft().topic())
                .partition(offset.getLeft().partition())
                .offset(offset.getRight())
                .build();
    }

    private Pair<TopicPartition, PartitionOffset> asOffset(TopicPartition topicPartition,
                                                           Optional<Pair<Long, Long>> offsetAndMetadata) {
        Long groupOffset = offsetAndMetadata.map(Pair::getKey).orElse(-1L);
        Long partitionOffset = offsetAndMetadata.map(Pair::getRight).orElse(-1L);

        return Pair.of(topicPartition, ImmutablePartitionOffset.builder()
                .groupOffset(groupOffset)
                .partitionOffset(partitionOffset)
                .lag(groupOffset > 0 ? partitionOffset - groupOffset : -1L)
                .build());
    }

    //Appears to be how the ConsumerGroupCommand.scala finds the offset/lag
    private CompletableFuture<Map<TopicPartition, Pair<Long, Long>>> withOffsets(KafkaConsumerResource<?, ?> resource,
                                                                                 Map<TopicPartition, OffsetAndMetadata> offsetData,
                                                                                 ConsumerGroupDescription description) {
        return FutureUtils.supplyAsync(() -> {
            List<String> topics = description.members().stream()
                    .flatMap(s -> s.assignment().topicPartitions().stream())
                    .map(TopicPartition::topic)
                    .distinct()
                    .collect(Collectors.toList());

            if (topics.isEmpty()) return Collections.emptyMap();

            Map<TopicPartition, Long> endOffsets = KafkaTaskUtils.subscribeAndSeek(resource, topics, Optional.empty(), Collections.emptyList()).getEndOffsets();

            return mapToOffset(offsetData, endOffsets);
        });
    }

    private Map<TopicPartition, Pair<Long, Long>> mapToOffset(Map<TopicPartition, OffsetAndMetadata> group,
                                                              Map<TopicPartition, Long> offsets) {
        return offsets.entrySet().stream()
                .map(tp -> new AbstractMap.SimpleEntry<>(tp.getKey(), Pair.of(metadataToLong(tp.getKey(), group), tp.getValue())))
                .collect(StreamUtils.mapCollector());
    }

    private Long metadataToLong(TopicPartition tp, Map<TopicPartition, OffsetAndMetadata> offsets) {
        return Optional.ofNullable(offsets.get(tp)).map(OffsetAndMetadata::offset).orElse(-1L);
    }
}
