package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.input.CreateTopicRequest;
import com.github.domwood.kiwi.data.input.UpdateTopicConfig;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.admin.CreateTopic;
import com.github.domwood.kiwi.kafka.task.admin.DeleteConsumerGroup;
import com.github.domwood.kiwi.kafka.task.admin.DeleteTopic;
import com.github.domwood.kiwi.kafka.task.admin.UpdateTopicConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.api.rest.utils.RestUtils.unEncodeParameter;
import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@Profile("write-admin")
@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class AdminWriteController {

    private final KafkaTaskProvider taskProvider;

    @Autowired
    public AdminWriteController(KafkaTaskProvider kafkaTaskProvider) {
        this.taskProvider = kafkaTaskProvider;
    }


    @Async
    @PostMapping("/createTopic")
    @ResponseBody
    public CompletableFuture<Void> createTopic(@RequestParam(required = false) Optional<String> bootStrapServers,
                                               @RequestBody CreateTopicRequest createTopicRequest) {
        CreateTopic createTopic = this.taskProvider.createTopic(createTopicRequest, bootStrapServers);
        return createTopic.execute();
    }

    @Async
    @DeleteMapping("/deleteTopic/{topic}")
    @ResponseBody
    public CompletableFuture<Void> deleteTopic(@RequestParam(required = false) Optional<String> bootStrapServers,
                                               @PathVariable String topic) {
        DeleteTopic deleteTopic = this.taskProvider.deleteTopic(unEncodeParameter(topic), bootStrapServers);
        return deleteTopic.execute();
    }

    @Async
    @DeleteMapping("/deleteConsumerGroup/{groupId}")
    @ResponseBody
    public CompletableFuture<Void> deleteConsumerGroup(@RequestParam(required = false) Optional<String> bootStrapServers,
                                                       @PathVariable String groupId) {
        DeleteConsumerGroup deleteConsumerGroup = this.taskProvider.deleteConsumerGroup(unEncodeParameter(groupId), bootStrapServers);
        return deleteConsumerGroup.execute();
    }

    @Async
    @PostMapping("/updateTopicConfig")
    @ResponseBody
    public CompletableFuture<Void> updateTopicConfig(@RequestParam(required = false) Optional<String> bootStrapServers,
                                                     @RequestBody UpdateTopicConfig topicConfig) {
        UpdateTopicConfiguration topicConfiguration = this.taskProvider.updateTopicConfiguration(topicConfig, bootStrapServers);
        return topicConfiguration.execute();
    }

}
