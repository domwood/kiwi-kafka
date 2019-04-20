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

    fetch(api.topicInfo + '?topic='+topic)
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

    fetch(api.logs + '?brokerId='+id)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.brokerLogInfo))
        .catch(errorhandler);
}

export const getConsumerGroupTopicDetails = (cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error);

    fetch(api.getConsumerGroupTopicDetails)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.topicDetails))
        .catch(errorhandler);
};;



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

export const consume = (topics, limit, fromStart, filter, cb, eb) => {
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
                topics: topics,
                limit: limit||1,
                limitAppliesFromStart: fromStart||false,
                filter: filter || null
            })
        })
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorhandler);
    }
};