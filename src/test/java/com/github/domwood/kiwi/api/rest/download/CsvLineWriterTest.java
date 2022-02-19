package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.ConsumerRequestColumns;
import com.github.domwood.kiwi.data.input.ConsumerRequestFileType;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import com.github.domwood.kiwi.testutils.TestDataFactory;
import org.junit.jupiter.api.Test;

import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.HEADERS;
import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.KEY;
import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.OFFSET;
import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.PARTITION;
import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.TIMESTAMP;
import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.VALUE;
import static com.github.domwood.kiwi.testutils.TestDataFactory.buildConsumedMessage;
import static com.github.domwood.kiwi.testutils.TestDataFactory.buildConsumerToFileRequest;
import static com.github.domwood.kiwi.testutils.TestDataFactory.testHeadersAsString;
import static com.github.domwood.kiwi.testutils.TestDataFactory.testKey;
import static com.github.domwood.kiwi.testutils.TestDataFactory.testOffset;
import static com.github.domwood.kiwi.testutils.TestDataFactory.testPartition;
import static com.github.domwood.kiwi.testutils.TestDataFactory.testPayload;
import static com.github.domwood.kiwi.testutils.TestDataFactory.testTimestamp;
import static com.github.domwood.kiwi.testutils.TestUtils.testMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvLineWriterTest {

    private final ObjectMapper mapper = testMapper();

    @Test
    public void testCsvWriteKey() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", KEY).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = testKey;

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWriteValue() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", VALUE).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = testPayload;

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWriteHeaders() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", HEADERS).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);

        assertEquals(testHeadersAsString, observed);
    }


    @Test
    public void testCsvWriteOffset() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", OFFSET).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = testOffset.toString();

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWritePartition() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", PARTITION).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = testPartition.toString();

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWriteTimeStamp() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", TIMESTAMP).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = testTimestamp.toString();

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWriteAll() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", ConsumerRequestColumns.values()).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);

        String expected = String.format("%s %s %s %s %s %s", testKey, testTimestamp, testPartition, testOffset, testHeadersAsString, testPayload);

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWriteAllCustomDelimiter() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, "||", ConsumerRequestColumns.values()).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);

        String expected = String.format("%s||%s||%s||%s||%s||%s", testKey, testTimestamp, testPartition, testOffset, testHeadersAsString, testPayload);

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWriteDefaultDelimiter() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, null, ConsumerRequestColumns.values()).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);

        String expected = String.format("%s\t%s\t%s\t%s\t%s\t%s", testKey, testTimestamp, testPartition, testOffset, testHeadersAsString, testPayload);

        assertEquals(expected, observed);
    }

    @Test
    public void testCsvWriteValueEscapehNewlines() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.CSV, " ", VALUE).build();
        CsvLineWriter lineWriter = new CsvLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage()
                .message(testPayload + "\n\nHelloWorld")
                .build();

        String observed = lineWriter.writeLine(message);
        String expected = testPayload + "\\n\\nHelloWorld";

        assertEquals(expected, observed);
    }
}
