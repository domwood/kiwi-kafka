package com.github.domwood.kiwi;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.domwood.kiwi.api.ws.KiwiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class KiwiConfig implements WebMvcConfigurer, WebSocketConfigurer {

    private final KiwiWebSocketHandler kiwiWebSocketHandler;

    @Autowired
    public KiwiConfig(KiwiWebSocketHandler kiwiWebSocketHandler) {
        this.kiwiWebSocketHandler = kiwiWebSocketHandler;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowCredentials(true)
        ;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customize(){
        return builder -> builder.modules(new GuavaModule(), new Jdk8Module());
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(kiwiWebSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}
