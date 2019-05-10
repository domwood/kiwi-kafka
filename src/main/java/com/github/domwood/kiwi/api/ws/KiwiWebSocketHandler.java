package com.github.domwood.kiwi.api.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domwood.kiwi.data.input.CloseTaskRequest;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.InboundRequest;
import com.github.domwood.kiwi.data.output.OutboundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

//TODO WIP POC
@Component
public class KiwiWebSocketHandler extends TextWebSocketHandler {

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
            if(inboundRequest instanceof ConsumerRequest){
                ConcurrentWebSocketSessionDecorator decorator = sessions.get(session.getId());
                consumerHandler.addConsumerTask(session.getId(), (ConsumerRequest) inboundRequest, this.sendTextMessage(decorator));
            }
            else if(inboundRequest instanceof CloseTaskRequest){
                consumerHandler.removeConsumerTask(session.getId());
            }
        } catch (IOException e) {
            logger.error("Failed to parse inbound websocket request "+ message.getPayload(), e);
        }
    }

    //Blocks until socket is available
    private Consumer<OutboundResponse> sendTextMessage(ConcurrentWebSocketSessionDecorator session){
        return (response) -> {
            try{
                while(session.getBufferSize() > 0){
                    Thread.sleep(50);
                }
                String payload = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(payload));
            }
            catch (JsonProcessingException e){
                logger.error("Failed to serialize response " + response, e);
            }
            catch (IOException e){
                logger.error("Failed to send response via websocket" + response, e);
            }
            catch (InterruptedException e){
                logger.error("Interrupted whilst awaiting socket availability", e);
            }
        };
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.sessions.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, 200, 100));
        logger.info("Websocket session established with id {} from {}", session.getId(), session.getRemoteAddress());
    }

    public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
        logger.error("Websocket transport error for session id "+session.getId()+" from "+ session.getRemoteAddress(), e);
        this.sessions.remove(session.getId());
        this.consumerHandler.removeConsumerTask(session.getId());
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Websocket session closed with id {} from {} with status {}", session.getId(), session.getRemoteAddress(), status);
        this.sessions.remove(session.getId());
        this.consumerHandler.removeConsumerTask(session.getId());
    }

}