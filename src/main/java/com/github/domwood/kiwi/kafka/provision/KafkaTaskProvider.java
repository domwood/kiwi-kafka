package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.CreateTopicRequest;
import com.github.domwood.kiwi.data.input.ProducerRequest;
import com.github.domwood.kiwi.data.input.UpdateTopicConfig;
import com.github.domwood.kiwi.kafka.resources.*;
import com.github.domwood.kiwi.kafka.task.admin.*;
import com.github.domwood.kiwi.kafka.task.config.CreateTopicConfig;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
import com.github.domwood.kiwi.kafka.task.producer.ProduceSingleMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaTaskProvider {

    private final KafkaResourceProvider resourceProvider;

    @Autowired
    public KafkaTaskProvider(KafkaResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    private KafkaConsumerResource<String, String> consumer(Optional<String> bootstrapServers){
        return this.resourceProvider.kafkaStringConsumerResource(bootstrapServers);
    }

    private KafkaProducerResource<String, String> producer(Optional<String> bootstrapServers){
        return this.resourceProvider.kafkaStringProducerResource(bootstrapServers);
    }

    private KafkaAdminResource admin(Optional<String> bootstrapServers){
        return this.resourceProvider.kafkaAdminResource(bootstrapServers);
    }

    private KafkaTopicConfigResource config(){
        return this.resourceProvider.kafkaTopicConfigResource();
    }

    private KafkaResourcePair<KafkaAdminResource, KafkaConsumerResource<String,String>> adminAndConsumer(Optional<String> bootStrapServers){
        return this.resourceProvider.kafkaAdminAndConsumer(bootStrapServers);
    }

    public BasicConsumeMessages basicConsumeMessages(ConsumerRequest input){
        return new BasicConsumeMessages(consumer(input.bootStrapServers()), input);
    }

    public ProduceSingleMessage produceSingleMessage(ProducerRequest input){
        return new ProduceSingleMessage(producer(input.bootStrapServers()), input);
    }

    public ListTopics listTopics(Optional<String> bootstrapServers){
        return new ListTopics(admin(bootstrapServers), null);
    }

    public TopicInformation topicInfo(String topic, Optional<String> bootstrapServers){
        return new TopicInformation(admin(bootstrapServers), topic);
    }

    public BrokerInformation brokerInformation(Optional<String> bootstrapServers){
        return new BrokerInformation(admin(bootstrapServers), null);
    }

    public BrokerLogInformation brokerLogInformation(Integer input, Optional<String> bootstrapServers){
        return new BrokerLogInformation(admin(bootstrapServers), input);
    }

    public CreateTopicConfig createTopicConfigOptions(){
        return new CreateTopicConfig(config(), null);
    }

    public CreateTopic createTopic(CreateTopicRequest topicRequest, Optional<String> bootstrapServers){
        return new CreateTopic(admin(bootstrapServers), topicRequest);
    }

    public ConsumerGroupInformation consumerGroups(Optional<String> bootstrapServers){
        return new ConsumerGroupInformation(admin(bootstrapServers), null);
    }

    public AllConsumerGroupDetails consumerGroupTopicInformation(Optional<String> bootstrapServers) {
        return new AllConsumerGroupDetails(admin(bootstrapServers), null);
    }

    public ConsumerGroupListByTopic consumerGroupListByTopic(String topic, Optional<String> bootstrapServers){
        return new ConsumerGroupListByTopic(admin(bootstrapServers), topic);
    }

    public ConsumerGroupDetailsWithOffset consumerGroupOffsetInformation(String groupId, Optional<String> bootstrapServers) {
        return new ConsumerGroupDetailsWithOffset(adminAndConsumer(bootstrapServers), groupId);
    }

    public ContinuousConsumeMessages continousConsumeMessages(ConsumerRequest request) {
        return new ContinuousConsumeMessages(consumer(request.bootStrapServers()), request);
    }

    public DeleteTopic deleteTopic(String topic, Optional<String> bootstrapServers) {
        return new DeleteTopic(admin(bootstrapServers), topic);
    }

    public DeleteConsumerGroup deleteConsumerGroup(String groupId, Optional<String> bootStrapServers) {
        return new DeleteConsumerGroup(admin(bootStrapServers), groupId);
    }

    public UpdateTopicConfiguration updateTopicConfiguration(UpdateTopicConfig topicConfig, Optional<String> bootStrapServers) {
        return new UpdateTopicConfiguration(admin(bootStrapServers), topicConfig);
    }
}
