package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class ConsumerController {

    private final KafkaTaskProvider taskProvider;

    @Autowired
    public ConsumerController(KafkaTaskProvider taskProvider) {
        this.taskProvider = taskProvider;
    }

    @Async
    @PostMapping("/consume")
    @ResponseBody
    public CompletableFuture<ConsumerResponse<String, String>> sendToTopic(@RequestBody ConsumerRequest request) {

        BasicConsumeMessages consumeMessages = taskProvider.basicConsumeMessages(request);
        return consumeMessages.execute();
    }

}
