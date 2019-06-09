package com.github.domwood.kiwi.exceptions;

public class WebSocketSendFailedException extends RuntimeException{
    public WebSocketSendFailedException(Throwable cause) {
        super("Failed to forward to websocket", cause);
    }
    public WebSocketSendFailedException(String message) {
        super(message);
    }
}
