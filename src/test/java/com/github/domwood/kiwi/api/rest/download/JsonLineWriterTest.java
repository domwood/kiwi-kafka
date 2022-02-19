package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.ConsumerRequestColumns;
import com.github.domwood.kiwi.data.input.ConsumerRequestFileType;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
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
import static com.github.domwood.kiwi.testutils.TestDataFactory.testTimestamp;
import static com.github.domwood.kiwi.testutils.TestUtils.testMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonLineWriterTest {

    private final ObjectMapper mapper = testMapper();

    @Test
    public void testJsonWriteKey() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", KEY).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Key\":\"" + testKey + "\"}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteValue() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", VALUE).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Value\":\"{\\\"key\\\":\\\"value\\\"}\"}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteHeaders() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", HEADERS).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Headers\":" + testHeadersAsString + "}";

        assertEquals(expected, observed);
    }


    @Test
    public void testJsonWriteOffset() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", OFFSET).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Offset\":" + testOffset + "}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWritePartition() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", PARTITION).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Partition\":" + testPartition + "}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteTimeStamp() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", TIMESTAMP).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Timestamp\":" + testTimestamp + "}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteAll() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", ConsumerRequestColumns.values()).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);

        String expected = "{" +
                "\"Key\":\"" + testKey + "\"," +
                "\"Timestamp\":" + testTimestamp + "," +
                "\"Partition\":" + testPartition + "," +
                "\"Offset\":" + testOffset + "," +
                "\"Headers\":" + testHeadersAsString + "," +
                "\"Value\":\"{\\\"key\\\":\\\"value\\\"}\"" +
                "}";

        assertEquals(expected, observed);
    }

}
