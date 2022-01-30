package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.ImmutablePartitionInfo;
import com.github.domwood.kiwi.data.output.ImmutableTopicConfigValue;
import com.github.domwood.kiwi.data.output.ImmutableTopicInfo;
import com.github.domwood.kiwi.data.output.PartitionInfo;
import com.github.domwood.kiwi.data.output.TopicConfigValue;
import com.github.domwood.kiwi.data.output.TopicInfo;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;
import com.github.domwood.kiwi.utilities.StreamUtils;
import com.google.common.collect.ImmutableSortedMap;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;
import static com.github.domwood.kiwi.utilities.FutureUtils.toCompletable;
import static com.github.domwood.kiwi.utilities.StreamUtils.extract;
import static com.github.domwood.kiwi.utilities.StreamUtils.maximum;
import static java.util.Collections.singletonList;

public class TopicInformation extends AbstractKafkaTask<String, TopicInfo, KafkaAdminResource> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TopicInformation(KafkaAdminResource resource, String input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<TopicInfo> delegateExecute() {
        try {
            ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, input);
            DescribeConfigsResult configsResult = resource.describeConfigs(singletonList(configResource));
            DescribeTopicsResult topicDescription = resource.describeTopics(singletonList(input));

            CompletableFuture<TopicInfo> topicInfo = toCompletable(topicDescription.values().get(input))
                    .thenApply(this::asTopicInfo);
            CompletableFuture<SortedMap<String, TopicConfigValue>> configuration = toCompletable(configsResult.values().get(configResource))
                    .thenApply(config -> toKeyValueMap(config.entries()));

            return topicInfo.thenCombine(configuration, this::mergeInConfiguration);
        } catch (Exception e) {
            logger.error("Failed to execute describe topics task for " + input, e);
            resource.discard();
            return failedFuture(e);
        }
    }

    private TopicInfo asTopicInfo(TopicDescription description) {
        return ImmutableTopicInfo.builder()
                .topic(description.name())
                .partitionCount(description.partitions().size())
                .replicaCount(maximum(description.partitions(), p -> p.replicas().size()))
                .partitions(extract(description.partitions(), this::asPartitionInfo))
                .permissions(extractAcls(description))
                .build();
    }

    public PartitionInfo asPartitionInfo(TopicPartitionInfo partition) {
        return ImmutablePartitionInfo.builder()
                .partition(partition.partition())
                .replicationFactor(partition.replicas().size())
                .replicas(asIdList(partition.replicas()))
                .isrs(asIdList(partition.isr()))
                .leader(Optional.ofNullable(partition.leader()).map(Node::id).orElse(-1))
                .build();
    }

    private List<Integer> asIdList(List<Node> nodes) {
        return extract(nodes, Node::id);
    }

    private SortedMap<String, TopicConfigValue> toKeyValueMap(Collection<ConfigEntry> configEntries) {
        ImmutableSortedMap.Builder<String, TopicConfigValue> sortedMap = ImmutableSortedMap.naturalOrder();
        configEntries.forEach(kv -> sortedMap.put(kv.name(), toTopicConfigValue(kv)));
        return sortedMap.build();
    }

    private TopicConfigValue toTopicConfigValue(ConfigEntry configEntry) {
        return ImmutableTopicConfigValue.builder()
                .configValue(configEntry.value())
                .configKey(configEntry.name())
                .configDescription(configEntry.documentation())
                .isDefault(configEntry.isDefault())
                .build();
    }

    private TopicInfo mergeInConfiguration(TopicInfo topicInfo, SortedMap<String, TopicConfigValue> config) {
        return ImmutableTopicInfo.builder()
                .from(topicInfo)
                .configuration(config)
                .build();
    }

    private List<String> extractAcls(TopicDescription description) {
        Set<AclOperation> aclOperations = description.authorizedOperations();
        if (aclOperations == null) {
            return singletonList("UNKNOWN");
        } else {
            return StreamUtils.extract(description.authorizedOperations(), Enum::name);
        }
    }

}
