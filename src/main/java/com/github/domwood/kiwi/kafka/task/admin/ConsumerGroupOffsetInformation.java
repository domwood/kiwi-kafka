package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ConsumerGroupOffset;
import com.github.domwood.kiwi.data.output.ConsumerGroupOffsetDetails;
import com.github.domwood.kiwi.data.output.ImmutableConsumerGroupOffset;
import com.github.domwood.kiwi.data.output.ImmutableConsumerGroupOffsetDetails;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.kafka.task.KafkaTaskUtils;
import com.github.domwood.kiwi.utilities.FutureUtils;
import com.github.domwood.kiwi.utilities.StreamUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;


public class ConsumerGroupOffsetInformation implements KafkaTask<String, ConsumerGroupOffsetDetails, Pair<KafkaAdminResource, KafkaConsumerResource<?,?>>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CompletableFuture<ConsumerGroupOffsetDetails> execute(Pair<KafkaAdminResource, KafkaConsumerResource<?, ?>> resource,
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
                .thenCombine(description, (offsets, consumerDescription) -> this.toOffsetDetails(groupId, offsets, consumerDescription));
    }

    private ConsumerGroupOffsetDetails toOffsetDetails(String groupId,
                                                       Map<TopicPartition, Pair<OffsetAndMetadata, Long>> partitionOffsetData,
                                                       ConsumerGroupDescription description) {
        return ImmutableConsumerGroupOffsetDetails.builder()
                .offsets(asByTopicAndByPartition(groupId, partitionOffsetData, description))
                .build();
    }

    private Map<String, List<ConsumerGroupOffset>> asByTopicAndByPartition(String groupId,
                                                                           Map<TopicPartition, Pair<OffsetAndMetadata, Long>> partitionOffsetData,
                                                                           ConsumerGroupDescription description) {
        return StreamUtils.extract(partitionOffsetData, this::asOffset)
                .stream()
                .map(builder -> builder
                        .groupId(groupId)
                        .groupState(description.state().name())
                        .coordinator(String.format("%s (%s:%s)", description.coordinator().id(), description.coordinator().host(), description.coordinator().port()))
                        .build())
                .collect(Collectors.groupingBy(ConsumerGroupOffset::topic));
    }

    private ImmutableConsumerGroupOffset.Builder asOffset(TopicPartition topicPartition, Pair<OffsetAndMetadata, Long> offsetAndMetadata) {
        return ImmutableConsumerGroupOffset.builder()
                .groupOffset(offsetAndMetadata.getKey().offset())
                .partitionOffset(offsetAndMetadata.getRight())
                .partition(topicPartition.partition())
                .lag(offsetAndMetadata.getRight() - offsetAndMetadata.getLeft().offset())
                .topic(topicPartition.topic());
    }

    //Appears to be how the ConsumerGroupCommand.scala finds the offset/lag
    private CompletableFuture<Map<TopicPartition, Pair<OffsetAndMetadata, Long>>> withOffsets(KafkaConsumerResource<?,?> resource, Map<TopicPartition, OffsetAndMetadata> offsetData){
        return FutureUtils.supplyAsync(() -> {
            List<String> topics = offsetData.keySet().stream().map(TopicPartition::topic).distinct().collect(toList());
            if(topics.isEmpty()) return Collections.emptyMap();

            resource.subscribe(topics);
            Set<TopicPartition> topicPartitionSet = resource.assignment();

            logger.debug("Consumer awaiting assignment for {} ...", topics);

            while(topicPartitionSet.isEmpty()){
                resource.poll(Duration.of(10, MILLIS));
                topicPartitionSet = resource.assignment();
            }

            Set<TopicPartition> partitions = KafkaTaskUtils.assignment(resource);
            return mapToOffset(offsetData, resource.endOffsets(partitions));
        });
    }

    private Map<TopicPartition, Pair<OffsetAndMetadata, Long>> mapToOffset(Map<TopicPartition, OffsetAndMetadata> group, Map<TopicPartition, Long> offsets){
        return group.entrySet().stream()
                .map(groupEntry -> new AbstractMap.SimpleEntry<>(groupEntry.getKey(), Pair.of(groupEntry.getValue(), offsets.getOrDefault(groupEntry.getKey(), 0L))))
                .collect(StreamUtils.mapCollector());
    }

}
