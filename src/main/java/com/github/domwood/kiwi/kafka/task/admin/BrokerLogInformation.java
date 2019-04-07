package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.utilities.FutureUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeLogDirsResult;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.StreamUtils.extract;
import static java.util.Collections.singletonList;

public class BrokerLogInformation implements KafkaTask<Integer, BrokerLogInfoList, KafkaAdminResource> {

    @Override
    public CompletableFuture<BrokerLogInfoList> execute(KafkaAdminResource resource, Integer input) {
        AdminClient adminClient = resource.provisionResource();
        DescribeLogDirsResult result = adminClient.describeLogDirs(singletonList(input));

        return FutureUtils.toCompletable(result.values().get(input))
                .thenApply(data -> ImmutableBrokerLogInfoList.builder()
                        .addAllBrokerLogInfo(handle(data))
                        .build());
    }


    private List<BrokerLogInfo> handle(Map<String, DescribeLogDirsResponse.LogDirInfo> data){
        return extract(data, this::fromMapEntry);
    }

    private BrokerLogInfo fromMapEntry(String kafkaLogName, DescribeLogDirsResponse.LogDirInfo infoForNode){
        return ImmutableBrokerLogInfo.builder()
                .logName(kafkaLogName)
                .errorType(infoForNode.error.name())
                .addAllTopicInfoList(extract(infoForNode.replicaInfos, this::fromMapEntry))
                .build();
    }


    private BrokerLogTopicInfo fromMapEntry(TopicPartition topicPartition, DescribeLogDirsResponse.ReplicaInfo replicaInfo){
        return ImmutableBrokerLogTopicInfo.builder()
                .topic(topicPartition.topic())
                .partition(topicPartition.partition())
                .lag(replicaInfo.offsetLag)
                .size(replicaInfo.size)
                .isFuture(replicaInfo.isFuture)
                .build();
    }

}
