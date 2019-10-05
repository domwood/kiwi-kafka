package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.ConsumerRequestFileType;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableConsumerPosition;
import com.github.domwood.kiwi.data.output.ImmutableConsumerResponse;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;

import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.KEY;
import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileDownloadWriterTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    PrintWriter outputStream;

    @Mock
    ContinuousConsumeMessages task;

    @Test
    public void basicFileCSVDownloadTest() {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", KEY).build();

        FileDownloadWriter writer = writer(request);

        ConsumerResponse<String, String> response = consumerResponse(50,2L, 1L);

        writer.accept(response);

        verify(outputStream, times(1)).println(testKey);
        verifyNoMoreInteractions(outputStream, task);
    }

    @Test
    public void basicFileJsonDownloadTest() {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", KEY).build();

        FileDownloadWriter writer = writer(request);

        ConsumerResponse<String, String> response = consumerResponse(50,2L, 1L);

        writer.accept(response);

        verify(outputStream, times(1)).println("{\"Key\":\""+testKey+"\"}");
        verifyNoMoreInteractions(outputStream, task);
    }

    @Test
    public void testCloseWhenEmpty() {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", KEY).build();

        FileDownloadWriter writer = writer(request);

        ConsumerResponse<String, String> response = consumerResponse(100,1L, 1L);

        writer.accept(response);


        verify(outputStream, times(1)).println(testKey);
        verify(outputStream, times(1)).flush();
        verify(outputStream, times(1)).close();
        verify(task, times(1)).close();

        verifyNoMoreInteractions(outputStream, task);
    }

    @Test
    public void testCloseWhenNoPositionSent() {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", KEY).build();

        FileDownloadWriter writer = writer(request);

        ConsumerResponse<String, String> response = buildConsumerResponse().build();

        writer.accept(response);

        verify(outputStream, times(1)).println(testKey);
        verify(outputStream, times(1)).flush();
        verify(outputStream, times(1)).close();
        verify(task, times(1)).close();

        verifyNoMoreInteractions(outputStream, task);
    }

    private FileDownloadWriter writer(ConsumerToFileRequest request){
        return new FileDownloadWriter(
                objectMapper,
                request,
                outputStream,
                task
        );
    }

    private ConsumerResponse<String, String> consumerResponse(Integer percentage, Long endValue, Long position){
        return ImmutableConsumerResponse.<String, String>builder()
                .from(buildConsumerResponse().build())
                .position(ImmutableConsumerPosition.builder()
                        .percentage(percentage)
                        .endValue(endValue)
                        .startValue(0L)
                        .consumerPosition(position)
                        .totalRecords(position.intValue())
                        .build())
                .build();
    }
}
