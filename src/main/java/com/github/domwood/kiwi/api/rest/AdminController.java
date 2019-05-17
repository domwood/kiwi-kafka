package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.input.CreateTopicRequest;
import com.github.domwood.kiwi.data.output.*;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.admin.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@RestController
@RequestMapping(API_ENDPOINT)
public class AdminController {

    private final KafkaResourceProvider resourceProvider;
    private final KafkaTaskProvider taskProvider;

    @Autowired
    public AdminController(KafkaTaskProvider kafkaTaskProvider,
                           KafkaResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.taskProvider = kafkaTaskProvider;
    }

    @Async
    @GetMapping("/listTopics")
    @ResponseBody
    public CompletableFuture<TopicList> listTopics(@RequestParam(required = false) Optional<String> bootStrapServers) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        ListTopics listTopics = this.taskProvider.listTopics();
        return listTopics.execute(adminResource, null);
    }

    @Async
    @GetMapping("/topicInfo/{topic}")
    @ResponseBody
    public CompletableFuture<TopicInfo> topicInfo(@RequestParam(required = false) Optional<String> bootStrapServers,
                                                  @PathVariable String topic) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        TopicInformation topicInformation = this.taskProvider.topicInfo();
        return topicInformation.execute(adminResource, topic);
    }

    @Async
    @GetMapping("/listConsumerGroups")
    @ResponseBody
    public CompletableFuture<ConsumerGroupList> consumerGroups(@RequestParam(required = false) Optional<String> bootStrapServers) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        ConsumerGroupInformation consumerGroupInformation = this.taskProvider.consumerGroups();
        return consumerGroupInformation.execute(adminResource, null);
    }

    @Async
    @GetMapping("/listConsumerGroupTopicDetails")
    @ResponseBody
    public CompletableFuture<ConsumerGroupTopicDetails> consumerGroupTopicDetails(@RequestParam(required = false) Optional<String> bootStrapServers) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        ConsumerGroupTopicInformation consumerGroupInformation = this.taskProvider.consumerGroupTopicInformation();
        return consumerGroupInformation.execute(adminResource, null);
    }

    @Async
    @GetMapping("/listConsumerGroupOffsetDetails/{groupId}")
    @ResponseBody
    public CompletableFuture<ConsumerGroupOffsetDetails> consumerGroupTopicDetails(@RequestParam(required = false) Optional<String> bootStrapServers,
                                                                                   @PathVariable String groupId) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        KafkaConsumerResource<String, String> consumerResource = resourceProvider.kafkaStringConsumerResource(bootStrapServers);
        ConsumerGroupOffsetInformation consumerGroupInformation = this.taskProvider.consumerGroupOffsetInformation();
        return consumerGroupInformation.execute(Pair.of(adminResource, consumerResource), groupId);
    }

    @Async
    @GetMapping("/brokers")
    @ResponseBody
    public CompletableFuture<BrokerInfoList> brokers(@RequestParam(required = false) Optional<String> bootStrapServers) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        BrokerInformation brokerInformation = this.taskProvider.brokerInformation();
        return brokerInformation.execute(adminResource, null);
    }

    @Async
    @GetMapping("/logs")
    @ResponseBody
    public CompletableFuture<BrokerLogInfoList> brokers(@RequestParam Integer brokerId,
                                                        @RequestParam(required = false) Optional<String> bootStrapServers) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        BrokerLogInformation brokerInformation = this.taskProvider.brokerLogInformation();
        return brokerInformation.execute(adminResource, brokerId);
    }

    @Async
    @PostMapping("/createTopic")
    @ResponseBody
    public CompletableFuture<Void> createTopic(@RequestParam(required = false) Optional<String> bootStrapServers,
                                               @RequestBody CreateTopicRequest createTopicRequest) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        CreateTopic createTopic = this.taskProvider.createTopic();
        return createTopic.execute(adminResource, createTopicRequest);
    }

    @Async
    @DeleteMapping("/deleteTopic/{topic}")
    @ResponseBody
    public CompletableFuture<Void> deleteTopic(@RequestParam(required = false) Optional<String> bootStrapServers,
                                               @PathVariable String topic) {
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        DeleteTopic deleteTopic = this.taskProvider.deleteTopic();
        return deleteTopic.execute(adminResource, topic);
    }
}
