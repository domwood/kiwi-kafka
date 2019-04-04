package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.kafka.task.admin.ListTopics;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import com.github.domwood.kiwi.kafka.task.producer.ProduceSingleMessage;
import org.springframework.stereotype.Component;

@Component
public class KafkaTaskProvider {

    public BasicConsumeMessages basicConsumeMessages(){
        return new BasicConsumeMessages();
    }

    public ProduceSingleMessage produceSingleMessage(){
        return new ProduceSingleMessage();
    }

    public ListTopics listTopics(){
        return new ListTopics();
    }
}
