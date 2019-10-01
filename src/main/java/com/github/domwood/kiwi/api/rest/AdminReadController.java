package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.api.rest.utils.RestUtils.unEncodeParameter;
import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@Profile("read-admin")
@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class AdminReadController {

    private final KafkaTaskProvider taskProvider;

    @Autowired
    public AdminReadController(KafkaTaskProvider kafkaTaskProvider) {
        this.taskProvider = kafkaTaskProvider;
    }

    @Async
    @GetMapping("/listTopics")
    @ResponseBody
    public CompletableFuture<TopicList> listTopics(@RequestParam(required = false) Optional<String> bootStrapServers) {
        ListTopics listTopics = this.taskProvider.listTopics(bootStrapServers);
        return listTopics.execute();
    }

    @Async
    @GetMapping("/topicInfo/{topic}")
    @ResponseBody
    public CompletableFuture<TopicInfo> topicInfo(@RequestParam(required = false) Optional<String> bootStrapServers,
                                                  @PathVariable String topic) {
        TopicInformation topicInformation = this.taskProvider.topicInfo(unEncodeParameter(topic), bootStrapServers);
        return topicInformation.execute();
    }

    @Async
    @GetMapping("/listConsumerGroups")
    @ResponseBody
    public CompletableFuture<ConsumerGroupList> consumerGroups(@RequestParam(required = false) Optional<String> bootStrapServers) {
        ConsumerGroupInformation consumerGroupInformation = this.taskProvider.consumerGroups(bootStrapServers);
        return consumerGroupInformation.execute();
    }

    @Async
    @GetMapping("/consumerGroupsForTopic/{topic}")
    @ResponseBody
    public CompletableFuture<ConsumerGroupList> consumerGroupsForTopic(@RequestParam(required = false) Optional<String> bootStrapServers,
                                                                       @PathVariable String topic) {
        ConsumerGroupListByTopic consumerGroupByTopic = this.taskProvider.consumerGroupListByTopic(unEncodeParameter(topic), bootStrapServers);
        return consumerGroupByTopic.execute();
    }

    @Async
    @GetMapping("/listAllConsumerGroupDetails")
    @ResponseBody
    public CompletableFuture<ConsumerGroups> consumerGroupTopicDetails(@RequestParam(required = false) Optional<String> bootStrapServers) {
        AllConsumerGroupDetails consumerGroupInformation = this.taskProvider.consumerGroupTopicInformation(bootStrapServers);
        return consumerGroupInformation.execute();
    }

    @Async
    @GetMapping("/listConsumerGroupDetailsWithOffsets/{groupId}")
    @ResponseBody
    public CompletableFuture<ConsumerGroupTopicWithOffsetDetails> consumerGroupTopicDetails(@RequestParam(required = false) Optional<String> bootStrapServers,
                                                                                            @PathVariable String groupId) {
        ConsumerGroupDetailsWithOffset consumerGroupInformation = this.taskProvider.consumerGroupOffsetInformation(unEncodeParameter(groupId), bootStrapServers);
        return consumerGroupInformation.execute();
    }

    @Async
    @GetMapping("/brokers")
    @ResponseBody
    public CompletableFuture<BrokerInfoList> brokers(@RequestParam(required = false) Optional<String> bootStrapServers) {
        BrokerInformation brokerInformation = this.taskProvider.brokerInformation(bootStrapServers);
        return brokerInformation.execute();
    }

    @Async
    @GetMapping("/logs")
    @ResponseBody
    public CompletableFuture<BrokerLogInfoList> brokers(@RequestParam Integer brokerId,
                                                        @RequestParam(required = false) Optional<String> bootStrapServers) {
        BrokerLogInformation brokerInformation = this.taskProvider.brokerLogInformation(brokerId, bootStrapServers);
        return brokerInformation.execute();
    }

}
