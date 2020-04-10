package com.github.domwood.kiwi.api.rest;

import com.github.domwood.kiwi.data.output.CreateTopicConfigOptions;
import com.github.domwood.kiwi.kafka.configs.KafkaConfigManager;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class ConfigController {

    private final KafkaTaskProvider taskProvider;
    private final KafkaConfigManager configManager;

    @Autowired
    public ConfigController(KafkaTaskProvider taskProvider, KafkaConfigManager configManager){
        this.taskProvider = taskProvider;
        this.configManager = configManager;
    }

    @Async
    @GetMapping("/createTopicConfig")
    @ResponseBody
    public CompletableFuture<CreateTopicConfigOptions> createTopicConfigOptions(){
        return this.taskProvider.createTopicConfigOptions().execute();
    }

    @GetMapping("/kafkaConfig")
    @ResponseBody
    public Map<String, Map<String, Map<String, String>>> getKafkaConfig(){
        return this.configManager.getClusterConfiguration();
    }

}
