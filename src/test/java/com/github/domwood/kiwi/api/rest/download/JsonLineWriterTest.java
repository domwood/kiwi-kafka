package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.ConsumerRequestColumns;
import com.github.domwood.kiwi.data.input.ConsumerRequestFileType;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;
import org.junit.jupiter.api.Test;

import static com.github.domwood.kiwi.data.input.ConsumerRequestColumns.*;
import static com.github.domwood.kiwi.testutils.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonLineWriterTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonWriteKey() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", KEY).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage<String, String> message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Key\":\""+testKey+"\"}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteValue() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", VALUE).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage<String, String> message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Value\":\"{\\\"key\\\":\\\"value\\\"}\"}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteHeaders() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", HEADERS).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage<String, String> message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String headers = mapper.writeValueAsString(testHeaders);
        String expected = "{\"Headers\":"+headers+"}";

        assertEquals(expected, observed);
    }


    @Test
    public void testJsonWriteOffset() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", OFFSET).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage<String, String> message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Offset\":"+testOffset+"}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWritePartition() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", PARTITION).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage<String, String> message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Partition\":"+testPartition+"}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteTimeStamp() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", TIMESTAMP).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage<String, String> message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);
        String expected = "{\"Timestamp\":"+testTimestamp+"}";

        assertEquals(expected, observed);
    }

    @Test
    public void testJsonWriteAll() throws JsonProcessingException {
        ConsumerToFileRequest request = buildConsumerToFileRequest(ConsumerRequestFileType.JSON, " ", ConsumerRequestColumns.values()).build();
        JsonLineWriter lineWriter = new JsonLineWriter(mapper, request);

        ConsumedMessage<String, String> message = buildConsumedMessage().build();

        String observed = lineWriter.writeLine(message);

        String headers = mapper.writeValueAsString(testHeaders);

        String expected = "{" +
                "\"Key\":\""+testKey+"\"," +
                "\"Timestamp\":"+testTimestamp+"," +
                "\"Partition\":"+testPartition+"," +
                "\"Offset\":"+testOffset+"," +
                "\"Headers\":"+headers+"," +
                "\"Value\":\"{\\\"key\\\":\\\"value\\\"}\"" +
        "}";

        assertEquals(expected, observed);
    }
    
}
