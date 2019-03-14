package com.github.domwood.kiwi.api;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@RestController
@RequestMapping(API_ENDPOINT)
public class ConsumerController {

    private final KafkaResourceProvider resourceProvider;

    @Autowired
    public ConsumerController(KafkaResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Async
    @PostMapping("/consume")
    @ResponseBody
    public CompletableFuture<List<ConsumerResponse<String, String>>> sendToTopic(@RequestParam(required = false) String bootStrapServers,
                                                                                 @RequestBody ConsumerRequest request) {

        KafkaConsumerResource<String, String> resource = resourceProvider.kafkaConsumerResource(bootStrapServers);
        BasicConsumeMessages consumeMessages = new BasicConsumeMessages();
        return consumeMessages.execute(resource, request);
    }

}
