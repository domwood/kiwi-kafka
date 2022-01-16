package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsOptions;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.DescribeLogDirsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.ConfigResource;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaAdminResource extends AbstractKafkaResource<AdminClient> {

    public KafkaAdminResource(Properties props) {
        super(props);
    }

    protected AdminClient createClient(ImmutableMap<Object, Object> props){
        Properties properties = new Properties();
        properties.putAll(props);
        return AdminClient.create(properties);
    }

    @Override
    protected void closeClient() throws KafkaResourceClientCloseException {
        try{
            this.getClient().close(Duration.ofSeconds(10));
        }
        catch (Exception e){
            throw new KafkaResourceClientCloseException("Kafka admin resource failed to close cleanly", e);
        }
    }

    public DescribeClusterResult describeCluster(){
        return this.getClient().describeCluster();
    }

    public DescribeLogDirsResult describeLogDirs(List<Integer> nodes){
        return this.getClient().describeLogDirs(nodes);
    }

    public DescribeConfigsResult describeConfigs(Collection<ConfigResource> resources) {
        return this.getClient().describeConfigs(resources, new DescribeConfigsOptions().includeDocumentation(true));
    }

    public DescribeTopicsResult describeTopics(Collection<String> topicNames) {
        return this.getClient().describeTopics(topicNames);
    }

    public ListConsumerGroupsResult listConsumerGroups() {
        return this.getClient().listConsumerGroups();
    }

    public DescribeConsumerGroupsResult describeConsumerGroups(Collection<String> groupIds) {
        return this.getClient().describeConsumerGroups(groupIds);
    }

    public CreateTopicsResult createTopics(Collection<NewTopic> newTopics) {
        return this.getClient().createTopics(newTopics);
    }

    public ListTopicsResult listTopics() {
        return this.getClient().listTopics();
    }

    public ListConsumerGroupOffsetsResult listConsumerGroupOffsets(String groupId){
        return this.getClient().listConsumerGroupOffsets(groupId);
    }

    public DeleteTopicsResult deleteTopics(List<String> topics) {
        return this.getClient().deleteTopics(topics);
    }

    public DeleteConsumerGroupsResult deleteConsumerGroups(List<String> groupIds) {
        return this.getClient().deleteConsumerGroups(groupIds);
    }

    public AlterConfigsResult updateTopicConfiguration(Map<ConfigResource, Config> configs){
        return this.getClient().alterConfigs(configs);
    }
}
