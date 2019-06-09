package com.github.domwood.kiwi.exceptions;

public class KafkaResourceClientCloseException extends Exception {
    public KafkaResourceClientCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
