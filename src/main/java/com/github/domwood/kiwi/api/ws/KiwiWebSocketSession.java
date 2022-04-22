package com.github.domwood.kiwi.api.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class KiwiWebSocketSession {
    private final ConcurrentWebSocketSessionDecorator delegate;
    private final AtomicBoolean isReady;
    private final AtomicReference<String> pending;

    public KiwiWebSocketSession(final WebSocketSession session,
                                final Long websocketBufferLimit) {
        this.delegate = new ConcurrentWebSocketSessionDecorator(session, 200, websocketBufferLimit.intValue());
        this.isReady = new AtomicBoolean(true);
        this.pending = new AtomicReference<>(null);
    }

    public boolean isReady() {
        return this.isReady.get();
    }

    public void setNotReady() {
        this.isReady.set(false);
    }

    public void setReady() {
        this.isReady.set(true);
    }

    public int getBufferSize() {
        return this.delegate.getBufferSize();
    }

    public boolean isOpen() {
        return this.delegate.isOpen();
    }

    public void sendMessage(String message) throws IOException {
        this.delegate.sendMessage(new TextMessage(message));
    }

    public void close(CloseStatus status) throws IOException {
        this.delegate.close(status);
    }

    public String getId() {
        return this.delegate.getId();
    }

    public void setPending(final String pending) {
        this.pending.set(pending);
    }

    public void sendPending() throws IOException {
        if (this.isReady.getAndSet(false)) {
            final String toEmit = this.pending.getAndSet(null);
            if (Objects.nonNull(toEmit) && this.isOpen()) {
                this.delegate.sendMessage(new TextMessage(toEmit));
            } else {
                this.setReady();
            }
        }
    }
}
