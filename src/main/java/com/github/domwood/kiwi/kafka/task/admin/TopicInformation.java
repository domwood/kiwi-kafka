package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ImmutablePartitionInfo;
import com.github.domwood.kiwi.data.output.ImmutableTopicInfo;
import com.github.domwood.kiwi.data.output.PartitionInfo;
import com.github.domwood.kiwi.data.output.TopicInfo;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.utilities.StreamUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static com.github.domwood.kiwi.utilities.StreamUtils.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

public class TopicInformation implements KafkaTask<String, TopicInfo, KafkaAdminResource> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CompletableFuture<TopicInfo> execute(KafkaAdminResource resource, String topic) {
        try{
            AdminClient adminClient = resource.provisionResource();
            ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
            DescribeConfigsResult configsResult = adminClient.describeConfigs(singletonList(configResource));
            DescribeTopicsResult topicDescription = adminClient.describeTopics(singletonList(topic));

            CompletableFuture<TopicInfo> topicInfo = toCompletable(topicDescription.values().get(topic))
                    .thenApply(this::asTopicInfo);
            CompletableFuture<Map<String, String>> configuration = toCompletable(configsResult.values().get(configResource))
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

    public Map<String, String> toKeyValueMap(Collection<ConfigEntry> configEntries){
        return configEntries.stream()
                .collect(toMap(ConfigEntry::name, ConfigEntry::value, StreamUtils::arbitrary));
    }

    public TopicInfo mergeInConfiguration(TopicInfo topicInfo, Map<String, String> config){
        return ImmutableTopicInfo.builder()
                .from(topicInfo)
                .configuration(config)
                .build();
    }

}
