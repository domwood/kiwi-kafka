package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaAdminResource extends KafkaResource<AdminClient>{

    public KafkaAdminResource(Properties props) {
        super(props);
    }

    protected AdminClient createClient(Properties props){
        return AdminClient.create(props);
    }

    @Override
    protected void closeClient() throws Exception {
        this.client.close(10, TimeUnit.SECONDS);
    }

    public DescribeClusterResult describeCluster(){
        return this.client.describeCluster();
    }

    public DescribeLogDirsResult describeLogDirs(List<Integer> nodes){
        return this.client.describeLogDirs(nodes);
    }

    public ListConsumerGroupsResult listConsumerGroups() {
        return this.client.listConsumerGroups();
    }

    public DescribeConsumerGroupsResult describeConsumerGroups(Collection<String> groupIds) {
        return this.client.describeConsumerGroups(groupIds);
    }

    public CreateTopicsResult createTopics(Collection<NewTopic> newTopics) {
        return this.client.createTopics(newTopics);
    }

    public ListTopicsResult listTopics() {
        return this.client.listTopics();
    }

    public DescribeConfigsResult describeConfigs(Collection<ConfigResource> resources) {
        return this.client.describeConfigs(resources);
    }

    public DescribeTopicsResult describeTopics(Collection<String> topicNames) {
        return this.client.describeTopics(topicNames);
    }

}
