package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.domwood.kiwi.data.input.ConsumerRequestColumns;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;

import java.util.Map;
import java.util.Set;

public class JsonLineWriter implements FileLineWriter{

    private final Set<ConsumerRequestColumns> columns;
    private final ObjectMapper mapper;

    public JsonLineWriter(ObjectMapper mapper,
                         ConsumerToFileRequest request){

        this.mapper = mapper;
        this.columns = request.columns();
    }

    @Override
    public String writeLine(ConsumedMessage message) throws JsonProcessingException {
        ConsumedMessageLine messageLine = new ConsumedMessageLine();
        if(columns.contains(ConsumerRequestColumns.KEY)){
            messageLine.setKey(String.valueOf(message.key()));
        }
        if(columns.contains(ConsumerRequestColumns.TIMESTAMP)){
            messageLine.setTimestamp(message.timestamp());
        }
        if(columns.contains(ConsumerRequestColumns.PARTITION)){
            messageLine.setPartition(message.partition());
        }
        if(columns.contains(ConsumerRequestColumns.OFFSET)){
            messageLine.setOffset(message.offset());
        }
        if(columns.contains(ConsumerRequestColumns.HEADERS)){
            messageLine.setHeaders(message.headers());
        }
        if(columns.contains(ConsumerRequestColumns.VALUE)){
            messageLine.setValue(String.valueOf(message.message()));
        }


        return mapper.writeValueAsString(messageLine);
    }


    @JsonSerialize
    private static class ConsumedMessageLine{
        private String Key;
        private Long Timestamp;
        private Integer Partition;
        private Long Offset;
        private Map<String, Object> Headers;
        private String Value;

        private ConsumedMessageLine(){
            this.Key = null;
            this.Timestamp = null;
            this.Partition = null;
            this.Offset = null;
            this.Headers = null;
            this.Value = null;
        }

        public void setKey(String key) {
            Key = key;
        }

        public void setTimestamp(Long timestamp) {
            Timestamp = timestamp;
        }

        public void setPartition(Integer partition) {
            Partition = partition;
        }

        public void setOffset(Long offset) {
            Offset = offset;
        }

        public void setHeaders(Map<String, Object> headers) {
            Headers = headers;
        }

        public void setValue(String value) {
            Value = value;
        }

        public String getKey() {
            return Key;
        }

        public Long getTimestamp() {
            return Timestamp;
        }

        public Integer getPartition() {
            return Partition;
        }

        public Long getOffset() {
            return Offset;
        }

        public Map<String, Object> getHeaders() {
            return Headers;
        }

        public String getValue() {
            return Value;
        }
    }
}
