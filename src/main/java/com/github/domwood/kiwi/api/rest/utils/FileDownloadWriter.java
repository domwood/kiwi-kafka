package com.github.domwood.kiwi.api.rest.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FileDownloadWriter implements Consumer<ConsumerResponse> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ServletOutputStream outputStream;
    private final ContinuousConsumeMessages task;
    private final ObjectMapper mapper;
    private final AtomicBoolean isClosed;

    public FileDownloadWriter(ObjectMapper mapper,
                              ServletOutputStream outputStream,
                              ContinuousConsumeMessages task){
        this.isClosed = new AtomicBoolean(false);
        this.mapper = mapper;
        this.outputStream = outputStream;
        this.task = task;
    }

    @Override
    public void accept(ConsumerResponse data) {
        try {
            outputStream.println(mapper.writeValueAsString(data));

            if(hasReachedEnd(data)){
                tryToClose();
            }
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private boolean hasReachedEnd(ConsumerResponse<String, String> data){
        return !data.position().isPresent() ||
                data.position().map(d -> d.endValue() <= d.consumerPosition()).orElse(false);
    }

    public void tryToClose(){
        if(this.isClosed.getAndSet(true)){
            try {
                logger.info("Finished Writing to file");
                this.isClosed.set(true);
                outputStream.close();
                task.close();
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
        }

    }

}
