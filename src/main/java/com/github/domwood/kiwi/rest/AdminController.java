package com.github.domwood.kiwi.rest;

import com.github.domwood.kiwi.api.output.TopicList;
import com.github.domwood.kiwi.kafka.provision.KafkaResourceProvider;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.task.admin.ListTopics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.CompletableFuture;

@Controller
public class AdminController {

    private final KafkaResourceProvider resourceProvider;

    @Autowired
    public AdminController(KafkaResourceProvider resourceProvider){
        this.resourceProvider = resourceProvider;
    }

    @Async
    @GetMapping("/admin/listTopics")
    @ResponseBody
    public CompletableFuture<TopicList> listTopics(@RequestParam(required = false) String bootStrapServers){
        KafkaAdminResource adminResource = resourceProvider.kafkaAdminResource(bootStrapServers);
        ListTopics listTopics = new ListTopics();
        return listTopics.execute(adminResource, null);
    }

}
