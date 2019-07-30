package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.file.ImmutableConsumedMessageLine;
import com.github.domwood.kiwi.data.input.ConsumerRequestColumns;
import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessage;

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
        ImmutableConsumedMessageLine.Builder messageLine = ImmutableConsumedMessageLine.builder();
        if(columns.contains(ConsumerRequestColumns.KEY)){
            messageLine.key(String.valueOf(message.key()));
        }
        if(columns.contains(ConsumerRequestColumns.TIMESTAMP)){
            messageLine.timestamp(message.timestamp());
        }
        if(columns.contains(ConsumerRequestColumns.PARTITION)){
            messageLine.partition(message.partition());
        }
        if(columns.contains(ConsumerRequestColumns.OFFSET)){
            messageLine.offset(message.offset());
        }
        if(columns.contains(ConsumerRequestColumns.HEADERS)){
            messageLine.headers(message.headers());
        }
        if(columns.contains(ConsumerRequestColumns.VALUE)){
            messageLine.value(String.valueOf(message.message()));
        }

        return mapper.writeValueAsString(messageLine.build());
    }

}
