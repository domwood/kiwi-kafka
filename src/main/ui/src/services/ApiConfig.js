/*eslint no-undef: "off"*/
const baseUrl = process.env.REACT_APP_LOCAL_SPRING_API ? process.env.REACT_APP_LOCAL_SPRING_API : 'localhost:8080';
const baseRestApi = baseUrl + "/api";
const baseWebSocket = baseUrl + "/ws";

export default {
    webSocket: `ws://${baseWebSocket}`,
    brokers: `http://${baseRestApi}/brokers`,
    logs: `http://${baseRestApi}/logs`,
    listTopics: `http://${baseRestApi}/listTopics`,
    topicInfo: `http://${baseRestApi}/topicInfo`,
    produce: `http://${baseRestApi}/produce`,
    consume: `http://${baseRestApi}/consume`,
    createTopic: `http://${baseRestApi}/createTopic`,
    createTopicConfig: `http://${baseRestApi}/createTopicConfig`,
    listConsumerGroupTopicDetails: `http://${baseRestApi}/listConsumerGroupTopicDetails`
}