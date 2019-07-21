package com.github.domwood.kiwi.data.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@JsonSerialize(as = ImmutableConsumerToFileRequest.class)
@JsonDeserialize(as = ImmutableConsumerToFileRequest.class)
@Value.Immutable
@Value.Style(depluralize = true)
public interface ConsumerToFileRequest extends AbstractConsumerRequest{
    ConsumerRequestFileType requestType();
    Optional<String> columnDelimiter();
    Set<ConsumerRequestColumns> columns();
}
