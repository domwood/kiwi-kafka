package com.github.domwood.kiwi;

import com.github.domwood.kiwi.api.ws.KiwiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Profile("read-consumer")
@EnableWebSocket
@Configuration
public class KiwiWebSocketConfig implements WebSocketConfigurer {

    private final KiwiWebSocketHandler kiwiWebSocketHandler;

    @Autowired
    public KiwiWebSocketConfig(KiwiWebSocketHandler kiwiWebSocketHandler) {
        this.kiwiWebSocketHandler = kiwiWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(kiwiWebSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}
