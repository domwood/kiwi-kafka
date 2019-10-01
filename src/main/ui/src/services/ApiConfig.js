/*eslint no-undef: "off"*/
const local = process.env.REACT_APP_ENV === 'local';
const baseWs = local? process.env.REACT_APP_LOCAL_SPRING_WS : `ws://${window.location.hostname}:${window.location.port}`;
const baseUrl = local? process.env.REACT_APP_LOCAL_SPRING_API : `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;
const baseRestApi = baseUrl + "/api";
const baseWebSocket = baseWs + "/ws";

console.log(`Local address as : ${baseUrl}`)

export default {
    webSocket: `${baseWebSocket}`,
    version: `${baseRestApi}/version`,
    profiles: `${baseRestApi}/profiles`,
    brokers: `${baseRestApi}/brokers`,
    logs: `${baseRestApi}/logs`,
    listTopics: `${baseRestApi}/listTopics`,
    topicInfo: `${baseRestApi}/topicInfo`,
    produce: `${baseRestApi}/produce`,
    consume: `${baseRestApi}/consume`,
    createTopic: `${baseRestApi}/createTopic`,
    deleteTopic: `${baseRestApi}/deleteTopic`,
    createTopicConfig: `${baseRestApi}/createTopicConfig`,
    consumerGroupsForTopic: `${baseRestApi}/consumerGroupsForTopic`,
    listConsumerGroups: `${baseRestApi}/listConsumerGroups`,
    listAllConsumerGroupDetails: `${baseRestApi}/listAllConsumerGroupDetails`,
    listConsumerGroupDetailsWithOffsets: `${baseRestApi}/listConsumerGroupDetailsWithOffsets`,
    deleteConsumerGroup: `${baseRestApi}/deleteConsumerGroup`,
    updateTopicConfig: `${baseRestApi}/updateTopicConfig`,
    consumeToFile: `${baseRestApi}/consumeToFile`
}