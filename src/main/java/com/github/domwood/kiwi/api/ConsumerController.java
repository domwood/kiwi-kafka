package com.github.domwood.kiwi.api;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@RestController
@RequestMapping(API_ENDPOINT)
public class ConsumerController {

    private final KafkaResourceProvider resourceProvider;
    private final KafkaTaskProvider taskProvider;

    @Autowired
    public ConsumerController(KafkaTaskProvider taskProvider,
                              KafkaResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.taskProvider = taskProvider;
    }

    @Async
    @PostMapping("/consume")
    @ResponseBody
    public CompletableFuture<ConsumerResponse<String, String>> sendToTopic(@RequestParam(required = false) String bootStrapServers,
                                                           @RequestBody ConsumerRequest request) {

        KafkaConsumerResource<String, String> resource = resourceProvider.kafkaStringConsumerResource(bootStrapServers);
        BasicConsumeMessages consumeMessages = taskProvider.basicConsumeMessages();
        return consumeMessages.execute(resource, request);
    }

}
