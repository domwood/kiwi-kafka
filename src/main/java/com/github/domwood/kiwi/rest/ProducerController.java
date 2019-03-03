package com.github.domwood.kiwi.rest;

import com.github.domwood.kiwi.api.input.ProducerRequest;
import com.github.domwood.kiwi.api.output.ProducerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
import com.github.domwood.kiwi.kafka.task.producer.ProduceSingleMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;


import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@RestController
@RequestMapping(API_ENDPOINT)
public class ProducerController {

    private final KafkaResourceProvider resourceProvider;

    @Autowired
    public ProducerController(KafkaResourceProvider resourceProvider){
        this.resourceProvider = resourceProvider;
    }

    @Async
    @PostMapping("/produce")
    @ResponseBody
    public CompletableFuture<ProducerResponse> sendToTopic(@RequestParam(required = false) String bootStrapServers,
                                                           @RequestBody ProducerRequest input){
        KafkaProducerResource<String, String> producerResource =
                resourceProvider.kafkaProducerResource(bootStrapServers);

        ProduceSingleMessage singleMessage = new ProduceSingleMessage();
        return singleMessage.execute(producerResource, input);
    }

}
