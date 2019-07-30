package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.api.rest.exception.KiwiFileDownloadException;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.github.domwood.kiwi.data.input.ConsumerRequestFileType.CSV;

public class FileDownloadWriter implements Consumer<ConsumerResponse<String, String>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PrintWriter outputStream;
    private final ContinuousConsumeMessages task;
    private final AtomicBoolean isClosed;
    private final FileLineWriter writer;

    public FileDownloadWriter(ObjectMapper mapper,
                              ConsumerToFileRequest request,
                              PrintWriter outputStream,
                              ContinuousConsumeMessages task){
        this.isClosed = new AtomicBoolean(false);
        this.outputStream = outputStream;
        this.task = task;
        this.writer = request.fileType().equals(CSV) ?
                new CsvLineWriter(mapper, request) : new JsonLineWriter(mapper, request);
    }

    @Override
    public void accept(ConsumerResponse<String, String> data) {
        try {
            for(ConsumedMessage message : data.messages()){
                outputStream.println(writer.writeLine(message));
            }
            if(hasReachedEnd(data)){
                tryToClose();
            }
        }
        catch (IOException e){
            throw new KiwiFileDownloadException("Failed to correctly execute file download", e);
        }
    }

    private boolean hasReachedEnd(ConsumerResponse<String, String> data){
        return !data.position().isPresent() ||
                data.position().map(d -> d.endValue() <= d.consumerPosition()).orElse(false);
    }

    public void tryToClose(){
        if(!this.isClosed.getAndSet(true)){
            try {
                logger.info("Finished Writing to file");
                outputStream.flush();
                outputStream.close();
            }
            catch (Exception e){
                logger.error("Failed to safely clean up stream");
            }
            try{
                task.close();
            }
            catch (Exception e){
                logger.error("Failed to safely clean up consumer");
            }
        }
    }

}
