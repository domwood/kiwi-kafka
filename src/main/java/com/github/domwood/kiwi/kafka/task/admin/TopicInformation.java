package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ImmutablePartitionInfo;
import com.github.domwood.kiwi.data.output.ImmutableTopicInfo;
import com.github.domwood.kiwi.data.output.PartitionInfo;
import com.github.domwood.kiwi.data.output.TopicInfo;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.google.common.collect.ImmutableSortedMap;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static com.github.domwood.kiwi.utilities.StreamUtils.extract;
import static com.github.domwood.kiwi.utilities.StreamUtils.maximum;
import static java.util.Collections.singletonList;

public class TopicInformation implements KafkaTask<String, TopicInfo, KafkaAdminResource> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CompletableFuture<TopicInfo> execute(KafkaAdminResource resource, String topic) {
        try{
            ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
            DescribeConfigsResult configsResult = resource.describeConfigs(singletonList(configResource));
            DescribeTopicsResult topicDescription = resource.describeTopics(singletonList(topic));

            CompletableFuture<TopicInfo> topicInfo = toCompletable(topicDescription.values().get(topic))
                    .thenApply(this::asTopicInfo);
            CompletableFuture<SortedMap<String, String>> configuration = toCompletable(configsResult.values().get(configResource))
                    .thenApply(config -> toKeyValueMap(config.entries()));

            return topicInfo.thenCombine(configuration, this::mergeInConfiguration);
        }
        catch (Exception e){
            logger.error("Failed to execute describe topics task for " + topic, e);
            resource.discard();
            return failedFuture(e);
        }
    }

    public TopicInfo asTopicInfo(TopicDescription description){
        return ImmutableTopicInfo.builder()
                .topic(description.name())
                .partitionCount(description.partitions().size())
                .replicaCount(maximum(description.partitions(), p -> p.replicas().size()))
                .partitions(extract(description.partitions(), this::asPartitionInfo))
                .build();
    }

    public PartitionInfo asPartitionInfo(TopicPartitionInfo partition){
            return ImmutablePartitionInfo.builder()
                    .partition(partition.partition())
                    .replicationfactor(partition.replicas().size())
                    .replicas(asIdList(partition.replicas()))
                    .isrs(asIdList(partition.isr()))
                    .leader(partition.leader().id())
                    .build();
    }

    public List<Integer> asIdList(List<Node> nodes){
        return extract(nodes, Node::id);
    }

    public SortedMap<String, String> toKeyValueMap(Collection<ConfigEntry> configEntries){
        ImmutableSortedMap.Builder<String, String> sortedMap = ImmutableSortedMap.naturalOrder();
        configEntries.stream().forEach(kv -> sortedMap.put(kv.name(), kv.value()));
        return sortedMap.build();
    }

    public TopicInfo mergeInConfiguration(TopicInfo topicInfo, SortedMap<String, String> config){
        return ImmutableTopicInfo.builder()
                .from(topicInfo)
                .configuration(config)
                .build();
    }

}
