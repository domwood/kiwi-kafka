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

import java.io.IOException;
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
    public void basicFileCSVDownloadTest() throws IOException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", KEY);

        FileDownloadWriter writer = new FileDownloadWriter(
                objectMapper,
                request,
                outputStream,
                task
        );

        ConsumerResponse<String, String> response = ImmutableConsumerResponse.<String, String>builder()
                .from(buildConsumerResponse())
                .position(ImmutableConsumerPosition.builder()
                        .percentage(50)
                        .endValue(2L)
                        .startValue(0L)
                        .consumerPosition(1L)
                        .totalRecords(1)
                        .build())
                .build();

        writer.accept(response);

        verify(outputStream, times(1)).println(testKey);
        verifyNoMoreInteractions(outputStream, task);
    }

    @Test
    public void basicFileJsonDownloadTest() throws IOException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", KEY);

        FileDownloadWriter writer = new FileDownloadWriter(
                objectMapper,
                request,
                outputStream,
                task
        );

        ConsumerResponse<String, String> response = ImmutableConsumerResponse.<String, String>builder()
                .from(buildConsumerResponse())
                .position(ImmutableConsumerPosition.builder()
                        .percentage(50)
                        .endValue(2L)
                        .startValue(0L)
                        .consumerPosition(1L)
                        .totalRecords(1)
                        .build())
                .build();

        writer.accept(response);

        verify(outputStream, times(1)).println("{\"Key\":\""+testKey+"\"}");
        verifyNoMoreInteractions(outputStream, task);
    }

    @Test
    public void testCloseWhenEmpty() throws IOException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", KEY);

        FileDownloadWriter writer = new FileDownloadWriter(
                objectMapper,
                request,
                outputStream,
                task
        );

        ConsumerResponse<String, String> response = ImmutableConsumerResponse.<String, String>builder()
                .from(buildConsumerResponse())
                .position(ImmutableConsumerPosition.builder()
                        .percentage(100)
                        .endValue(1L)
                        .startValue(0L)
                        .consumerPosition(1L)
                        .totalRecords(1)
                        .build())
                .build();

        writer.accept(response);


        verify(outputStream, times(1)).println(testKey);
        verify(outputStream, times(1)).flush();
        verify(outputStream, times(1)).close();
        verify(task, times(1)).close();

        verifyNoMoreInteractions(outputStream, task);
    }
}
