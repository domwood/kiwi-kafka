/*eslint no-undef: "off"*/
const baseUrl = process.env.REACT_APP_LOCAL_SPRING_API ? process.env.REACT_APP_LOCAL_SPRING_API : 'api/';

export default {
    brokers: baseUrl + "brokers",
    logs: baseUrl + "logs",
    listTopics: baseUrl + "listTopics",
    topicInfo: baseUrl + "topicInfo",
    produce: baseUrl + "produce",
    consume: baseUrl + "consume",
    createTopic: baseUrl + "createTopic",
    createTopicConfig: baseUrl + "createTopicConfig"
}