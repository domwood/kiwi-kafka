package com.github.domwood.kiwi;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.domwood.kiwi.data.serialization.CustomHeaderMapSerializer;
import com.github.domwood.kiwi.data.serialization.PairDeserializer;
import com.github.domwood.kiwi.data.serialization.PairSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class KiwiConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowCredentials(true)
        ;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer restObjectMapperCustomisation() {
        return builder -> builder.modules(customModules());
    }

    public static com.fasterxml.jackson.databind.Module[] customModules() {
        return new com.fasterxml.jackson.databind.Module[]{
                new GuavaModule(),
                new Jdk8Module(),
                new SimpleModule()
                        .addDeserializer(Pair.class, new PairDeserializer())
                        .addSerializer(Pair.class, new PairSerializer())
                        .addSerializer(CustomHeaderMapSerializer.CustomerHeaderMap.class, new CustomHeaderMapSerializer())
        };
    }

}
