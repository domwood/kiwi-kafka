package com.github.domwood.kiwi.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestWebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(TestWebSocketClient.class);
    private final AtomicBoolean webSocketOpen;
    private final ConcurrentLinkedQueue<WebSocketMessage<String>> received;
    private final ConcurrentLinkedQueue<Throwable> errors;
    private final StandardWebSocketClient client;
    private final TestWebSocketHandler handler;

    public TestWebSocketClient() {
        this.webSocketOpen = new AtomicBoolean(false);
        this.received = new ConcurrentLinkedQueue<>();
        this.errors = new ConcurrentLinkedQueue<>();
        this.client = new StandardWebSocketClient();
        this.handler = new TestWebSocketHandler(webSocketOpen, received, errors);
    }

    public void connect(String uri) {
        client.doHandshake(handler, uri);
    }

    public boolean isOpen() {
        return this.webSocketOpen.get();
    }

    public ConcurrentLinkedQueue<WebSocketMessage<String>> getReceived() {
        return this.received;
    }

    public ConcurrentLinkedQueue<Throwable> getErrors() {
        return this.errors;
    }

    public void send(String payload) throws IOException {
        this.handler.send(payload);
    }

    private static class TestWebSocketHandler implements WebSocketHandler {

        private final AtomicBoolean webSocketOpen;
        private final ConcurrentLinkedQueue<WebSocketMessage<String>> received;
        private final ConcurrentLinkedQueue<Throwable> errors;
        private WebSocketSession session;

        private TestWebSocketHandler(AtomicBoolean webSocketOpen, ConcurrentLinkedQueue<WebSocketMessage<String>> received, ConcurrentLinkedQueue<Throwable> errors) {
            this.webSocketOpen = webSocketOpen;
            this.received = received;
            this.errors = errors;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            logger.error("[TEST WEBSOCKET CLIENT] CONNECTED");
            this.session = session;
            this.webSocketOpen.set(true);
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            this.received.add((WebSocketMessage<String>) message);
            logger.debug("[TEST WEBSOCKET CLIENT] RECEIVED <= {}", message.getPayload());
            this.send("{\"requestType\": \".MessageAcknowledge\"}");
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            logger.error("[TEST WEBSOCKET CLIENT] ERROR", exception);
            this.errors.add(exception);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            this.session = null;
            this.webSocketOpen.set(false);
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

        private void send(String payload) throws IOException {
            this.session.sendMessage(new TextMessage(payload));
            logger.debug("[TEST WEBSOCKET CLIENT] SENT => {}", payload);
        }
    }
}