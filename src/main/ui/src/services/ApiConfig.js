/*eslint no-undef: "off"*/
const baseUrl = process.env.REACT_APP_LOCAL_SPRING_API ? process.env.REACT_APP_LOCAL_SPRING_API : 'api';

export default {
    webSocket: `ws://${baseUrl}/ws`,
    brokers: `http://${baseUrl}/brokers`,
    logs: `http://${baseUrl}/logs`,
    listTopics: `http://${baseUrl}/listTopics`,
    topicInfo: `http://${baseUrl}/topicInfo`,
    produce: `http://${baseUrl}/produce`,
    consume: `http://${baseUrl}/consume`,
    createTopic: `http://${baseUrl}/createTopic`,
    createTopicConfig: `http://${baseUrl}/createTopicConfig`,
    listConsumerGroupTopicDetails: `http://${baseUrl}/listConsumerGroupTopicDetails`
}