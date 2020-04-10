import api from "./ApiConfig";
import {toast} from "react-toastify";
import SessionStore from "./SessionStore";

const doubleEncode = (param) => {
    return encodeURIComponent(encodeURIComponent(param))
};

const addClusterNameParam = (apiPath, appendParam) => {
    let activeCluster = SessionStore.getActiveCluster();
    if (activeCluster) {
        return apiPath + `${!appendParam ? '?' : '&'}clusterName=${activeCluster}`;
    } else return apiPath;
};

const errorHandler = (error) => {
    console.error(error);
    toast.error(error);
};

const statusHandler = (response) => {
    if (response.ok) {
        return response;
    } else {
        throw new Error(`${response.status} Error response from Server`);
    }
};

export const getVersion = (cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(api.version)
        .then(statusHandler)
        .then(res => res.text())
        .then(result => cb(result))
        .catch(errorHandler);
};

export const getProfiles = (cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(api.profiles)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorHandler);
};

export const getKafkaConfiguration = (cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(api.kafkaConfig)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorHandler);
};

export const getTopics = (cb, eb) => {
    let errorhandler = (error) => (eb || errorHandler)(error);

    return fetch(addClusterNameParam(api.listTopics))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.topics))
        .catch(errorhandler);
};

export const getTopicInfo = (topic, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(`${api.topicInfo}/${doubleEncode(topic)}`))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorHandler);
};

export const getBrokers = (cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(api.brokers))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.brokerInfo))
        .catch(errorHandler);
};

export const getLogs = (id, cb, eb) => {
    let errorhandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(`${api.logs}?brokerId=${doubleEncode(id)}`, true))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.brokerLogInfo))
        .catch(errorhandler);
};


export const getConsumerGroups = (cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(api.listConsumerGroups))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.groups))
        .catch(errorHandler);
};


export const deleteConsumerGroup = (groupId, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(`${api.deleteConsumerGroup}/${doubleEncode(groupId)}`), {
        method: 'DELETE',
    })
        .then(statusHandler)
        .then(() => cb())
        .catch(errorHandler);
};

export const updateTopicConfig = (topic, config, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(api.updateTopicConfig), {
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
        .catch(errorHandler);
};

export const getConsumerGroupsForTopic = (topic, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(`${api.consumerGroupsForTopic}/${doubleEncode(topic)}`))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.groups))
        .catch(errorHandler);
};


export const getConsumerGroupDetailsWithOffsets = (groupdId, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(`${api.listConsumerGroupDetailsWithOffsets}/${doubleEncode(groupdId)}`))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.offsets))
        .catch(errorHandler);
};

export const getCreateTopicConfig = (cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(api.createTopicConfig))
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.configOptions))
        .catch(errorHandler);
};

export const createTopic = (topicData, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    topicData.requestType = ".CreateTopicRequest";

    fetch(addClusterNameParam(api.createTopic), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(topicData)
    })
        .then(statusHandler)
        .then(() => cb())
        .catch(errorHandler);
};

export const deleteTopic = (topic, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(addClusterNameParam(`${api.deleteTopic}/${doubleEncode(topic)}`), {
        method: 'DELETE',
    })
        .then(statusHandler)
        .then(() => cb())
        .catch(errorHandler);
};


export const produce = (topic, key, value, headers, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    if (!topic) {
        errorHandler(new Error("Topic must be defined to produce to kafka"));
    } else if (!key) {
        errorHandler(new Error("Key must be defined to produce to kafka"));
    } else {
        fetch(api.produce, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                clusterName: SessionStore.getActiveCluster(),
                requestType: ".ProducerRequest",
                topic: topic,
                key: key,
                headers: headers || {},
                payload: value || null
            })
        })
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result))
            .catch(errorHandler);
    }

};

export const consume = (topics, limit, fromStart, filters, cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error, "major");

    if (!topics) {
        errorHandler(new Error("Topic must be defined to consume from kafka"));
    } else {
        fetch(api.consume, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                clusterName: SessionStore.getActiveCluster(),
                requestType: ".ConsumerRequest",
                topics: topics,
                limit: limit || 1,
                limitAppliesFromStart: fromStart || false,
                filters: filters || []
            })
        })
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result))
            .catch(errorHandler);
    }
};

export const consumeToFile = (topics, filters, fileType, columns, separator, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error, "major");

    if (!topics) {
        errorHandler(new Error("Topic must be defined to consume from kafka"));
    } else {
        let data = JSON.stringify({
            clusterName: SessionStore.getActiveCluster(),
            requestType: ".ConsumerToFileRequest",
            topics: topics,
            limit: -1,
            limitAppliesFromStart: false,
            filters: filters || [],
            fileType: fileType,
            columns: columns,
            separator: separator
        });
        let encoded = window.btoa(data);
        let request = `${api.consumeToFile}?request=${encodeURIComponent(encoded)}`;
        window.open(request, "_blank");
    }
};