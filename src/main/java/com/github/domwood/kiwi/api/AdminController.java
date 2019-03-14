package com.github.domwood.kiwi.api;

import com.github.domwood.kiwi.data.output.TopicList;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.admin.ListTopics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@RestController
@RequestMapping(API_ENDPOINT)
public class AdminController {

    private final KafkaResourceProvider resourceProvider;

    @Autowired
    public AdminController(KafkaResourceProvider resourceProvider){
        this.resourceProvider = resourceProvider;
    }

    @Async
    @GetMapping("/listTopics")
    @ResponseBody
    public CompletableFuture<TopicList> listTopics(@RequestParam(required = false) String bootStrapServers){
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        ListTopics listTopics = new ListTopics();
        return listTopics.execute(adminResource, null);
    }

}
