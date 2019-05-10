package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.output.CreateTopicConfigOptions;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaTopicConfigResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@RestController
@RequestMapping(API_ENDPOINT)
public class ConfigController {

    private final KafkaResourceProvider resourceProvider;
    private final KafkaTaskProvider taskProvider;

    @Autowired
    public ConfigController(KafkaResourceProvider resourceProvider, KafkaTaskProvider taskProvider){
        this.resourceProvider = resourceProvider;
        this.taskProvider = taskProvider;
    }

    @Async
    @GetMapping("/createTopicConfig")
    @ResponseBody
    public CompletableFuture<CreateTopicConfigOptions> createTopicConfigOptions(){
        KafkaTopicConfigResource configResource = resourceProvider.kafkaTopicConfigResource();
        return this.taskProvider.createTopicConfigOptions().execute(configResource, null);
    }

}
