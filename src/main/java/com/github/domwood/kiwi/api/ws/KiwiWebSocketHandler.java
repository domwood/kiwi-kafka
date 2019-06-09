package com.github.domwood.kiwi.api.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.CloseTaskRequest;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.InboundRequest;
import com.github.domwood.kiwi.data.output.OutboundResponse;
import com.github.domwood.kiwi.exceptions.WebSocketSendFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//TODO WIP
@Component
public class KiwiWebSocketHandler extends TextWebSocketHandler {

    @Value("${websocket.max.wait.ms:10000}")
    Long maxWaitTime;

    @Value("${websocket.wait.interval.ms:10}")
    Long waitInterval;

    @Value("${websocket.message.buffer.limit:200}")
    Integer websocketBufferLimit;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper;
    private final KiwiWebSocketConsumerHandler consumerHandler;
    private final Map<String, ConcurrentWebSocketSessionDecorator> sessions;

    @Autowired
    public KiwiWebSocketHandler(ObjectMapper objectMapper, KiwiWebSocketConsumerHandler consumerHandler) {
        this.objectMapper = objectMapper;
        this.consumerHandler = consumerHandler;
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void handleTextMessage(WebSocketSession session,
                                  TextMessage message) {

        try {
            InboundRequest inboundRequest = objectMapper.readValue(message.getPayload(), InboundRequest.class);
            if (inboundRequest instanceof ConsumerRequest) {
                ConcurrentWebSocketSessionDecorator decorator = sessions.get(session.getId());
                consumerHandler.addConsumerTask(session.getId(), (ConsumerRequest) inboundRequest, this.sendTextMessage(decorator));
            } else if (inboundRequest instanceof CloseTaskRequest) {
                consumerHandler.removeConsumerTask(session.getId());
                if(((CloseTaskRequest) inboundRequest).closeSession()){
                    tryCloseSession(sessions.get(session.getId()), CloseStatus.NORMAL);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to parse inbound websocket request " + message.getPayload(), e);
        }
    }

    private Consumer<OutboundResponse> sendTextMessage(ConcurrentWebSocketSessionDecorator session) {
        return (OutboundResponse response) -> {
            try {
                String payload = objectMapper.writeValueAsString(response);

                maybeBlockConsumer(session);

                if(!session.isOpen()) throw new WebSocketSendFailedException("Session closed whilst data pending send");

                session.sendMessage(new TextMessage(payload));
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize response " + response, e);
                tryCloseSession(session);
                throw new WebSocketSendFailedException(e);
            } catch (IOException e) {
                logger.error("Failed to send response via websocket" + response, e);
                tryCloseSession(session);
                throw new WebSocketSendFailedException(e);
            }
        };
    }


    //TODO Find a cleaner blocking implementation
    @SuppressWarnings("squid:S2142") //Ignored for now
    private void maybeBlockConsumer(ConcurrentWebSocketSessionDecorator session){
        try{
            int sleeps = 0;
            long maxWaitCount = this.maxWaitTime / this.waitInterval;
            while (session.getBufferSize() >= websocketBufferLimit-1 &&
                    sleeps++ < maxWaitCount &&
                    session.isOpen()) {
                //Blocks upstream if socket is backlogged (ie will block kafka consumer polling further)
                Thread.sleep(10);
            }
            if(sleeps >= maxWaitCount) throw new WebSocketSendFailedException("Websocket blocked for too long, reached max wait");
        } catch (InterruptedException e) {
            logger.error("Interrupted whilst awaiting socket availability", e);
            tryCloseSession(session);
            throw new WebSocketSendFailedException(e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.sessions.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, 200, websocketBufferLimit));
        logger.info("Websocket session established with id {} from {}", session.getId(), session.getRemoteAddress());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
        logger.error("Websocket transport error for session id " + session.getId() + " from " + session.getRemoteAddress(), e);
        this.sessions.remove(session.getId());
        this.consumerHandler.removeConsumerTask(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Websocket session closed with id {} from {} with status {}", session.getId(), session.getRemoteAddress(), status);
        this.sessions.remove(session.getId());
        this.consumerHandler.removeConsumerTask(session.getId());
    }

    private void tryCloseSession(ConcurrentWebSocketSessionDecorator session){
        this.tryCloseSession(session, CloseStatus.SERVER_ERROR);
    }

    private void tryCloseSession(ConcurrentWebSocketSessionDecorator session, CloseStatus reason){
        try {
            session.close(reason);
        }
        catch(Exception e){
            logger.warn("Failed to cleanly close session after error", e);
        }
        finally {
            //Should be done on session close, be in the event the session is not closed properly
            this.consumerHandler.removeConsumerTask(session.getId());
        }
    }

}