package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.output.CreateTopicConfigOptions;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class ConfigController {

    private final KafkaTaskProvider taskProvider;

    @Autowired
    public ConfigController(KafkaTaskProvider taskProvider){
        this.taskProvider = taskProvider;
    }

    @Async
    @GetMapping("/createTopicConfig")
    @ResponseBody
    public CompletableFuture<CreateTopicConfigOptions> createTopicConfigOptions(){
        return this.taskProvider.createTopicConfigOptions().execute();
    }

}
