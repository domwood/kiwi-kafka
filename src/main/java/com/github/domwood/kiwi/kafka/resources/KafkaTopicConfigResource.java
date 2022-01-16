package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.config.TopicConfig;

import java.util.Properties;

public class KafkaTopicConfigResource extends AbstractKafkaResource<TopicConfig> {
    public KafkaTopicConfigResource(Properties props) {
        super(props);
    }

    @Override
    protected TopicConfig createClient(ImmutableMap<Object, Object> props) {
        return new TopicConfig();
    }

    @Override
    protected void closeClient() throws KafkaResourceClientCloseException {
        //Do nothing
    }

    public TopicConfig getConfig(){
        return this.getClient();
    }
}
