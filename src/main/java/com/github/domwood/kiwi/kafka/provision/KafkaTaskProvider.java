package com.github.domwood.kiwi.kafka.provision;

import com.github.domwood.kiwi.data.input.AbstractConsumerRequest;
import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.input.CreateTopicRequest;
import com.github.domwood.kiwi.data.input.ProducerRequest;
import com.github.domwood.kiwi.data.input.UpdateTopicConfig;
import com.github.domwood.kiwi.kafka.resources.KafkaAdminResource;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaDataTypeHandler;
import com.github.domwood.kiwi.kafka.resources.KafkaProducerResource;
import com.github.domwood.kiwi.kafka.resources.KafkaResourcePair;
import com.github.domwood.kiwi.kafka.resources.KafkaTopicConfigResource;
import com.github.domwood.kiwi.kafka.task.admin.AllConsumerGroupDetails;
import com.github.domwood.kiwi.kafka.task.admin.BrokerInformation;
import com.github.domwood.kiwi.kafka.task.admin.BrokerLogInformation;
import com.github.domwood.kiwi.kafka.task.admin.ConsumerGroupDetailsWithOffset;
import com.github.domwood.kiwi.kafka.task.admin.ConsumerGroupInformation;
import com.github.domwood.kiwi.kafka.task.admin.ConsumerGroupListByTopic;
import com.github.domwood.kiwi.kafka.task.admin.CreateTopic;
import com.github.domwood.kiwi.kafka.task.admin.DeleteConsumerGroup;
import com.github.domwood.kiwi.kafka.task.admin.DeleteTopic;
import com.github.domwood.kiwi.kafka.task.admin.ListTopics;
import com.github.domwood.kiwi.kafka.task.admin.TopicInformation;
import com.github.domwood.kiwi.kafka.task.admin.UpdateTopicConfiguration;
import com.github.domwood.kiwi.kafka.task.config.CreateTopicConfig;
import com.github.domwood.kiwi.kafka.task.consumer.BasicConsumeMessages;
import com.github.domwood.kiwi.kafka.task.consumer.ContinuousConsumeMessages;
import com.github.domwood.kiwi.kafka.task.producer.ProduceSingleMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.github.domwood.kiwi.kafka.resources.KafkaDataTypeHandlerProvider.getTypeHandler;

@Component
public class KafkaTaskProvider {

    private final KafkaResourceProvider resourceProvider;

    @Autowired
    public KafkaTaskProvider(KafkaResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @SuppressWarnings("unchecked")
    private <K, V> KafkaConsumerResource<K, V> consumer(AbstractConsumerRequest input) {
        KafkaDataTypeHandler<K> keyHandler = (KafkaDataTypeHandler<K>) getTypeHandler(input.kafkaKeyDataType());
        KafkaDataTypeHandler<V> valueHandler = (KafkaDataTypeHandler<V>) getTypeHandler(input.kafkaKeyDataType());
        return this.resourceProvider.kafkaConsumerResource(input.clusterName(), keyHandler, valueHandler);
    }

    @SuppressWarnings("unchecked")
    private <K, V> KafkaProducerResource<K, V> producer(ProducerRequest input) {
        KafkaDataTypeHandler<K> keyHandler = (KafkaDataTypeHandler<K>) getTypeHandler(input.kafkaKeyDataType());
        KafkaDataTypeHandler<V> valueHandler = (KafkaDataTypeHandler<V>) getTypeHandler(input.kafkaKeyDataType());
        return this.resourceProvider.kafkaProducerResource(input.clusterName(), keyHandler, valueHandler);
    }

    private KafkaAdminResource admin(Optional<String> clusterName) {
        return this.resourceProvider.kafkaAdminResource(clusterName);
    }

    private KafkaTopicConfigResource config() {
        return this.resourceProvider.kafkaTopicConfigResource();
    }

    private KafkaResourcePair<KafkaAdminResource, KafkaConsumerResource<String, String>> adminAndConsumer(Optional<String> clusterName) {
        return this.resourceProvider.kafkaAdminAndConsumer(clusterName);
    }

    public <K, V> BasicConsumeMessages<K, V> basicConsumeMessages(ConsumerRequest input) {
        return new BasicConsumeMessages<>(consumer(input), input);
    }

    public <K, V> ProduceSingleMessage<K, V> produceSingleMessage(ProducerRequest input) {
        return new ProduceSingleMessage<>(producer(input), input);
    }

    public ListTopics listTopics(Optional<String> bootstrapServers) {
        return new ListTopics(admin(bootstrapServers), null);
    }

    public TopicInformation topicInfo(String topic, Optional<String> bootstrapServers) {
        return new TopicInformation(admin(bootstrapServers), topic);
    }

    public BrokerInformation brokerInformation(Optional<String> bootstrapServers) {
        return new BrokerInformation(admin(bootstrapServers), null);
    }

    public BrokerLogInformation brokerLogInformation(Integer input, Optional<String> bootstrapServers) {
        return new BrokerLogInformation(admin(bootstrapServers), input);
    }

    public CreateTopicConfig createTopicConfigOptions() {
        return new CreateTopicConfig(config(), null);
    }

    public CreateTopic createTopic(CreateTopicRequest topicRequest, Optional<String> bootstrapServers) {
        return new CreateTopic(admin(bootstrapServers), topicRequest);
    }

    public ConsumerGroupInformation consumerGroups(Optional<String> bootstrapServers) {
        return new ConsumerGroupInformation(admin(bootstrapServers), null);
    }

    public AllConsumerGroupDetails consumerGroupTopicInformation(Optional<String> bootstrapServers) {
        return new AllConsumerGroupDetails(admin(bootstrapServers), null);
    }

    public ConsumerGroupListByTopic consumerGroupListByTopic(String topic, Optional<String> bootstrapServers) {
        return new ConsumerGroupListByTopic(admin(bootstrapServers), topic);
    }

    public ConsumerGroupDetailsWithOffset consumerGroupOffsetInformation(String groupId, Optional<String> bootstrapServers) {
        return new ConsumerGroupDetailsWithOffset(adminAndConsumer(bootstrapServers), groupId);
    }

    public <K, V> ContinuousConsumeMessages<K, V> continuousConsumeMessages(AbstractConsumerRequest request) {
        return new ContinuousConsumeMessages<>(consumer(request), request);
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
