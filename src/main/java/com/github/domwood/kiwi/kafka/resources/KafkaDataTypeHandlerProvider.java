package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.data.input.KafkaDataType;

import java.util.function.Function;

public class KafkaDataTypeHandlerProvider {

    private KafkaDataTypeHandlerProvider() {}

    public static KafkaDataTypeHandler<?> getTypeHandler(KafkaDataType kafkaDataType){
        switch (kafkaDataType){
            case STRING:
            default:
                return KafkaDataTypeHandlerProvider.stringTypeHandler();
        }
    }

    private static KafkaDataTypeHandler<String> stringTypeHandler(){
        return new KafkaDataTypeHandler<>(
                Function.identity(),
                Function.identity(),
                "org.apache.kafka.common.serialization.StringSerializer",
                "org.apache.kafka.common.serialization.StringDeserializer"
        );
    }


}
