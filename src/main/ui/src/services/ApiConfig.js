/*eslint no-undef: "off"*/
const baseWs = process.env.REACT_APP_LOCAL_SPRING_WS || 'ws://localhost:8080';
const baseUrl = process.env.REACT_APP_LOCAL_SPRING_API || 'http://localhost:8080';
const baseRestApi = baseUrl + "/api";
const baseWebSocket = baseWs + "/ws";

export default {
    webSocket: `${baseWebSocket}`,
    brokers: `${baseRestApi}/brokers`,
    logs: `${baseRestApi}/logs`,
    listTopics: `${baseRestApi}/listTopics`,
    topicInfo: `${baseRestApi}/topicInfo`,
    produce: `${baseRestApi}/produce`,
    consume: `${baseRestApi}/consume`,
    createTopic: `${baseRestApi}/createTopic`,
    deleteTopic: `${baseRestApi}/deleteTopic`,
    createTopicConfig: `${baseRestApi}/createTopicConfig`,
    listConsumerGroupTopicDetails: `${baseRestApi}/listConsumerGroupTopicDetails`,
    listConsumerGroupOffsetDetails: `${baseRestApi}/listConsumerGroupOffsetDetails`
}