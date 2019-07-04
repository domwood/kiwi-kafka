package com.github.domwood.kiwi.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.ImmutableConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.OutboundResponseWithPosition;
import com.github.domwood.kiwi.kafka.provision.KafkaTaskProvider;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class ConsumerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    @GetMapping(value = "/consumeToFile/{topic}", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public CompletableFuture getTopic(@PathVariable String topic, HttpServletResponse response) throws IOException {

        ContinuousConsumeMessages consumeMessages = taskProvider.continousConsumeMessages(ImmutableConsumerRequest.builder()
                .topics(singletonList(topic))
                .limit(-1)
                .limitAppliesFromStart(true)
                .filters(emptyList())
                .build());

        response.setContentType("application/force-download");
        ServletOutputStream outputStream = response.getOutputStream();

        consumeMessages.registerConsumer(data -> {
            if(!data.position().isPresent() ||
                    data.position().map(d -> d.endValue() <= d.consumerPosition()).orElse(false)){
                try {
                    outputStream.println(mapper.writeValueAsString(data));

                    logger.info("Finished Writing to file");
                    outputStream.close();
                    consumeMessages.close();
                }
                catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
            else{
                try {
                    outputStream.println(mapper.writeValueAsString(data));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return consumeMessages.execute();
    }

}
