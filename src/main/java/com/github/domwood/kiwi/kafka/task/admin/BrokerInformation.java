package com.github.domwood.kiwi.kafka.task.admin;

import com.github.domwood.kiwi.data.output.BrokerInfo;
import com.github.domwood.kiwi.data.output.BrokerInfoList;
import com.github.domwood.kiwi.data.output.ImmutableBrokerInfo;
import com.github.domwood.kiwi.data.output.ImmutableBrokerInfoList;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.AbstractKafkaTask;
import com.github.domwood.kiwi.utilities.FutureUtils;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.domwood.kiwi.utilities.StreamUtils.extract;

public class BrokerInformation extends AbstractKafkaTask<Void, BrokerInfoList, KafkaAdminResource> {

    public BrokerInformation(KafkaAdminResource resource, Void input) {
        super(resource, input);
    }

    @Override
    protected CompletableFuture<BrokerInfoList> delegateExecute() {
        DescribeClusterResult clusterResult = resource.describeCluster();

        return FutureUtils.toCompletable(clusterResult.nodes(), 30, TimeUnit.SECONDS)
                .thenApply(nodes -> ImmutableBrokerInfoList.builder()
                        .addAllBrokerInfo(extract(nodes, this::fromNode))
                        .build());
    }

    private BrokerInfo fromNode(Node node) {
        return ImmutableBrokerInfo.builder()
                .nodeNumber(node.id())
                .nodeName(node.idString())
                .host(node.host())
                .port(node.port())
                .nodeRack(node.rack())
                .build();
    }

}
