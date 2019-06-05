package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.input.ProducerRequest;
import com.github.domwood.kiwi.data.output.ProducerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.producer.ProduceSingleMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class ProducerController {

    private final KafkaTaskProvider taskProvider;

    @Autowired
    public ProducerController(KafkaTaskProvider taskProvider){
        this.taskProvider = taskProvider;
    }

    @Async
    @PostMapping(value = "/produce")
    @ResponseBody
    public CompletableFuture<ProducerResponse> sendToTopic(@RequestBody ProducerRequest input){
        ProduceSingleMessage singleMessage = taskProvider.produceSingleMessage(input);
        return singleMessage.execute();
    }

}
