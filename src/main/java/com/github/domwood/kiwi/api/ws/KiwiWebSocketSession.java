package com.github.domwood.kiwi.api.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class KiwiWebSocketSession {
    private ConcurrentWebSocketSessionDecorator delegate;
    private AtomicBoolean isReady;

    public KiwiWebSocketSession(WebSocketSession session, int websocketBufferLimit){
        this.delegate =  new ConcurrentWebSocketSessionDecorator(session, 200, websocketBufferLimit);
        this.isReady = new AtomicBoolean(true);
    }

    public boolean isReady(){
        return this.isReady.get();
    }

    public void setNotReady(){
        this.isReady.set(false);
    }

    public void setReady(){
        this.isReady.set(true);
    }

    public int getBufferSize(){
        return this.delegate.getBufferSize();
    }

    public boolean isOpen(){
        return this.delegate.isOpen();
    }

    public void sendMessage(String message) throws IOException{
        this.delegate.sendMessage(new TextMessage(message));
    }

    public void close(CloseStatus status) throws IOException {
        this.delegate.close(status);
    }

    public String getId(){
        return this.delegate.getId();
    }
}
