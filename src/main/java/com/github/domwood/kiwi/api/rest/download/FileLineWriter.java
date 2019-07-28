package com.github.domwood.kiwi.api.rest.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.domwood.kiwi.data.output.ConsumedMessage;

public interface FileLineWriter {
    public String writeLine(ConsumedMessage message) throws JsonProcessingException ;
}
