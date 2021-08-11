package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.config.ConfigResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TopicInformationTest {

    @Mock
    KafkaAdminResource resource;

    @Mock
    DescribeConfigsResult configsResult;

    @Mock
    DescribeTopicsResult topicsResult;

    @Mock
    KafkaFuture<Config> configFuture;

    @Mock
    KafkaFuture<TopicDescription> topicFuture;

    private final String topicName = "testtopic";

    private final Node leaderNode = new Node(46, "host", 9090);

    private final Config config = new Config(topicConfiguration.values().stream()
            .map(topicConfigValue -> new ConfigEntry(topicConfigValue.configKey(), topicConfigValue.configValue(), true, false, false))
            .collect(toList()));

    private final List<TopicPartitionInfo> nodes = IntStream.range(0, partitionCount).boxed()
            .map((i) -> new TopicPartitionInfo(i, leaderNode, singletonList(leaderNode), singletonList(leaderNode)))
            .collect(toList());

    private final TopicDescription topicDescription = new TopicDescription(topicName, false, nodes, null);

    @BeforeEach
    public void beforeEach() {
        when(resource.describeConfigs(anyList())).thenReturn(configsResult);
        when(resource.describeTopics(anyList())).thenReturn(topicsResult);

        when(configsResult.values()).thenReturn(ImmutableMap.of(
                new ConfigResource(ConfigResource.Type.TOPIC, topicName), configFuture
        ));

        when(topicsResult.values()).thenReturn(ImmutableMap.of(
                topicName, topicFuture
        ));
    }

    @DisplayName("Returns a list of topic information")
    @Test
    public void testTopicInformation() throws InterruptedException, ExecutionException, TimeoutException {
        when(configFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(config);
        when(topicFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(topicDescription);

        TopicInformation task = new TopicInformation(resource, topicName);

        TopicInfo observed = task.execute().get(10, TimeUnit.SECONDS);

        TopicInfo expected = buildTopicInfo(topicName).build();

        assertEquals(expected, observed);
    }

}
