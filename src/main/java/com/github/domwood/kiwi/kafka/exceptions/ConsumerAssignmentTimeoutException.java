package com.github.domwood.kiwi.kafka.exceptions;

public class ConsumerAssignmentTimeoutException extends RuntimeException{
    public ConsumerAssignmentTimeoutException(String message) {
        super(message);
    }
}
