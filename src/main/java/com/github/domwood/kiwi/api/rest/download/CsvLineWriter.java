package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.ConsumerRequestColumns;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;

import java.util.Set;

public class CsvLineWriter implements FileLineWriter {

    private final Set<ConsumerRequestColumns> columns;
    private final String delimiter;
    private final ObjectMapper mapper;

    public CsvLineWriter(ObjectMapper mapper,
                         ConsumerToFileRequest request){

        this.mapper = mapper;
        this.columns = request.columns();
        this.delimiter = request.columnDelimiter().orElse("\t");
    }

    public String writeLine(ConsumedMessage message) throws JsonProcessingException {
        StringBuilder buffer = new StringBuilder();
        writeKey(message, buffer);
        writeTimestamp(message, buffer);
        writePartition(message, buffer);
        writeOffset(message, buffer);
        writeHeaders(message, buffer);
        writeValue(message, buffer);

        return buffer.toString();
    }

    private void writeOffset(ConsumedMessage message, StringBuilder writer) {
        if(columns.contains(ConsumerRequestColumns.OFFSET)){
            if(writer.length() > 0) writer.append(delimiter);
            writer.append(message.offset());
        }
    }

    private void writePartition(ConsumedMessage message, StringBuilder writer) {
        if(columns.contains(ConsumerRequestColumns.PARTITION)){
            if(writer.length() > 0) writer.append(delimiter);
            writer.append(message.partition());
        }
    }

    private void writeTimestamp(ConsumedMessage message, StringBuilder writer) {
        if(columns.contains(ConsumerRequestColumns.TIMESTAMP)){
            if(writer.length() > 0) writer.append(delimiter);
            writer.append(message.timestamp());
        }
    }

    private void writeKey(ConsumedMessage message, StringBuilder writer){
        if(columns.contains(ConsumerRequestColumns.KEY)){
            if(writer.length() > 0) writer.append(delimiter);
            writer.append(message.key());
        }
    }

    private void writeValue(ConsumedMessage message, StringBuilder writer){
        if(columns.contains(ConsumerRequestColumns.VALUE)){
            if(writer.length() > 0) writer.append(delimiter);
            writer.append(escapeNewLines(message.message()));
        }
    }

    private void writeHeaders(ConsumedMessage message, StringBuilder writer) throws JsonProcessingException {
        if(columns.contains(ConsumerRequestColumns.HEADERS)){
            if(writer.length() > 0) writer.append(delimiter);
            writer.append(escapeNewLines(mapper.writeValueAsString(message.headers())));
        }
    }

    private String escapeNewLines(String input){
        if(input == null) return null;
        return input.replaceAll("\n", "\\\\n");
    }

}
