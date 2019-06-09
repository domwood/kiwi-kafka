package com.github.domwood.kiwi.exceptions;

public class ConsumerDataTooLargeException extends RuntimeException {
    public ConsumerDataTooLargeException(){
        super("Consumer Data too large, resubmit request with smaller limit");
    }
}
