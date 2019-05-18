package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.kafka.task.admin.*;
import com.github.domwood.kiwi.kafka.task.config.CreateTopicConfig;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
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

    public TopicInformation topicInfo(){
        return new TopicInformation();
    }

    public BrokerInformation brokerInformation(){
        return new BrokerInformation();
    }

    public BrokerLogInformation brokerLogInformation(){
        return new BrokerLogInformation();
    }

    public CreateTopicConfig createTopicConfigOptions(){
        return new CreateTopicConfig();
    }

    public CreateTopic createTopic(){
        return new CreateTopic();
    }

    public ConsumerGroupInformation consumerGroups(){
        return new ConsumerGroupInformation();
    }

    public AllConsumerGroupDetails consumerGroupTopicInformation() {
        return new AllConsumerGroupDetails();
    }

    public ConsumerGroupListByTopic consumerGroupListByTopic(){
        return new ConsumerGroupListByTopic();
    }

    public ConsumerGroupDetailsWithOffset consumerGroupOffsetInformation() {
        return new ConsumerGroupDetailsWithOffset();
    }

    public ContinuousConsumeMessages continousConsumeMessages() {
        return new ContinuousConsumeMessages();
    }

    public DeleteTopic deleteTopic() {
        return new DeleteTopic();
    }
}
