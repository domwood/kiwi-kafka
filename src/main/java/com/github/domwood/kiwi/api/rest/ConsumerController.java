package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.api.rest.utils.FileDownloadWriter;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class ConsumerController {
    private final KafkaTaskProvider taskProvider;
    private final ObjectMapper mapper;

    @Autowired
    public ConsumerController(KafkaTaskProvider taskProvider, ObjectMapper mapper) {
        this.taskProvider = taskProvider;
        this.mapper = mapper;
    }

    @Async
    @PostMapping("/consume")
    @ResponseBody
    public CompletableFuture<ConsumerResponse<String, String>> sendToTopic(@RequestBody ConsumerRequest request) {

        BasicConsumeMessages consumeMessages = taskProvider.basicConsumeMessages(request);
        return consumeMessages.execute();
    }

    @PostMapping(value = "/consumeToFile", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public CompletableFuture consumeTopicDataToFile(@RequestBody ConsumerToFileRequest request, HttpServletResponse response) throws IOException {

        ContinuousConsumeMessages consumeMessages = taskProvider.continousConsumeMessages(request);

        response.setContentType("application/force-download");
        ServletOutputStream outputStream = response.getOutputStream();

        FileDownloadWriter writer = new FileDownloadWriter(mapper, outputStream, consumeMessages);

        consumeMessages.registerConsumer(writer);

        return consumeMessages.execute()
                .thenRun(() -> writer.tryToClose());
    }

}
