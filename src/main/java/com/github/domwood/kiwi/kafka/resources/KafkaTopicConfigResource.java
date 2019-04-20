package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.common.config.TopicConfig;

import java.util.Properties;

public class KafkaTopicConfigResource extends KafkaResource<TopicConfig>{
    public KafkaTopicConfigResource(Properties props) {
        super(props);
    }

    @Override
    protected TopicConfig createClient(Properties props) {
        return new TopicConfig();
    }

    @Override
    protected void closeClient() throws Exception {
        //Do nothing
    }
}
