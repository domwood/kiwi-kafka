package com.github.domwood.kiwi.api;

import com.github.domwood.kiwi.data.output.TopicList;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
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
    private final KafkaTaskProvider taskProvider;

    @Autowired
    public AdminController(KafkaTaskProvider kafkaTaskProvider,
                           KafkaResourceProvider resourceProvider){
        this.resourceProvider = resourceProvider;
        this.taskProvider = kafkaTaskProvider;
    }

    @Async
    @GetMapping("/listTopics")
    @ResponseBody
    public CompletableFuture<TopicList> listTopics(@RequestParam(required = false) String bootStrapServers){
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        ListTopics listTopics = this.taskProvider.listTopics();
        return listTopics.execute(adminResource, null);
    }

}
