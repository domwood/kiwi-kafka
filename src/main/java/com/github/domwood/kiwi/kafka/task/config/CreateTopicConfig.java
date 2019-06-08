package com.github.domwood.kiwi.kafka.task.config;

import com.github.domwood.kiwi.data.output.CreateTopicConfigOptions;
import com.github.domwood.kiwi.data.output.ImmutableCreateTopicConfigOptions;
import com.github.domwood.kiwi.kafka.resources.KafkaTopicConfigResource;
import com.github.domwood.kiwi.kafka.task.FuturisingAbstractKafkaTask;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateTopicConfig extends FuturisingAbstractKafkaTask<Void, CreateTopicConfigOptions, KafkaTopicConfigResource> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CreateTopicConfig(KafkaTopicConfigResource resource, Void input) {
        super(resource, input);
    }

    @Override
    protected CreateTopicConfigOptions delegateExecuteSync() {
        TopicConfig topicConfig = resource.getConfig();
        Set<String> configs = Stream.of(topicConfig.getClass().getFields())
                .filter(field -> field.getName().endsWith("_CONFIG"))
                .map(field -> getValue(field, topicConfig))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return ImmutableCreateTopicConfigOptions.builder()
                .configOptions(configs)
                .build();
    }

    private String getValue(Field configField, TopicConfig configObj){
        try {
            return String.valueOf(configField.get(configObj));
        } catch (IllegalAccessException e) {
            logger.error("Failed to access field {} on kafka topic config", configField.getName());
        }
        return null;
    }
}
