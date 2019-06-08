package com.github.domwood.kiwi.kafka.exceptions;

public class KafkaResourceClientCloseException extends Exception {
    public KafkaResourceClientCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
