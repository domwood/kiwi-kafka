import api from "./ApiConfig";
import {toast} from "react-toastify";

const errorHandler = (error) => {
    console.error(error);
    toast.error(error);
};

const statusHandler = (response) => {
    if(response.ok){
        return response;
    }
    else{
        throw new Error(`${response.status} Error response from Server`);
    }
};

export const getTopics = (cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(api.listTopics)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.topics))
        .catch(errorhandler);
};

export const getTopicInfo = (topic, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(`${api.topicInfo}/${topic}`)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorhandler);
};

export const getBrokers = (cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(api.brokers)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.brokerInfo))
        .catch(errorhandler);
};

export const getLogs = (id, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(`${api.logs}?brokerId=${id}`)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.brokerLogInfo))
        .catch(errorhandler);
};

export const getAllConsumerGroupDetails = (cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(api.listConsumerGroupTopicDetails)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.topicDetails))
        .catch(errorhandler);
};

export const getConsumerGroups = (cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(`${api.listConsumerGroups}`)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.groups))
        .catch(errorhandler);
};


export const deleteConsumerGroup = (groupId, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(`${api.deleteConsumerGroup}/${groupId}`, {
        method: 'DELETE',
    })
    .then(statusHandler)
    .then(() => cb())
    .catch(errorhandler);
};

export const updateTopicConfig = (topic, config, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(api.updateTopicConfig, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            requestType: ".UpdateTopicConfig",
            topic: topic,
            config: config
        })
    })
        .then(statusHandler)
        .then(() => cb())
        .catch(errorhandler);
};

export const getConsumerGroupsForTopic = (topic, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(`${api.consumerGroupsForTopic}/${topic}`)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.groups))
        .catch(errorhandler);
};


export const getConsumerGroupDetailsWithOffsets = (groupdId, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(`${api.listConsumerGroupDetailsWithOffsets}/${groupdId}`)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.offsets))
        .catch(errorhandler);
};

export const getCreateTopicConfig = (cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(api.createTopicConfig)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.configOptions))
        .catch(errorhandler);
};

export const createTopic = (topicData, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    topicData.requestType = ".CreateTopicRequest";

    fetch(api.createTopic, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(topicData)
    })
    .then(statusHandler)
    .then(() => cb())
    .catch(errorhandler);
};

export const deleteTopic = (topic, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(`${api.deleteTopic}/${topic}`, {
        method: 'DELETE',
    })
    .then(statusHandler)
    .then(() => cb())
    .catch(errorhandler);
};


export const produce = (topic, key, value, headers, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    if(!topic){
        errorhandler(new Error("Topic must be defined to produce to kafka"));
    }
    else if(!key){
        errorhandler(new Error("Key must be defined to produce to kafka"));
    }
    else{
        fetch(api.produce, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                requestType: ".ProducerRequest",
                topic: topic,
                key: key,
                headers: headers||{},
                payload: value||null
            })
        })
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorhandler);
    }

};

export const consume = (topics, limit, fromStart, filters, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error, "major");

    if(!topics){
        errorhandler(new Error("Topic must be defined to consume from kafka"));
    }
    else{
        fetch(api.consume, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                requestType: ".ConsumerRequest",
                topics: topics,
                limit: limit||1,
                limitAppliesFromStart: fromStart||false,
                filters: filters || []
            })
        })
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorhandler);
    }
};