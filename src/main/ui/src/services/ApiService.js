import api from "./ApiConfig";
import {toast} from "react-toastify";
import SessionStore from "./SessionStore";

const doubleEncode = (param) => {
    return encodeURIComponent(encodeURIComponent(param))
};

const addClusterNameParam = (cb, eb) => {
    SessionStore.getActiveCluster((activeCluster) => {
        cb(activeCluster);
    }, eb);
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

const commonTextFetch = (cb, eb, apiEndpoint) => {
    let onError = (error) => (eb || errorHandler)(error);

    fetch(apiEndpoint)
        .then(statusHandler)
        .then(res => res.text())
        .then(result => cb(result))
        .catch(onError);
}

export const getVersion = (cb, eb) => {
    commonTextFetch(cb, eb, api.version);
};

export const getProfiles = (cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    fetch(api.profiles)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result))
        .catch(onError);
};

export const getTopics = (cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        return fetch(`${api.listTopics}?clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result.topics))
            .catch(onError);
    }, onError);
};

export const getTopicInfo = (topic, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.topicInfo}/${doubleEncode(topic)}?clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result))
            .catch(onError);
    }, onError)

};

export const getBrokers = (cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.brokers}?clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result.brokerInfo))
            .catch(onError);
    }, onError);

};

export const getLogs = (id, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.logs}?brokerId=${doubleEncode(id)}&clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result.brokerLogInfo))
            .catch(onError);
    }, onError)

};


export const getConsumerGroups = (cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.listConsumerGroups}?clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result.groups))
            .catch(onError);
    }, onError);

};


export const deleteConsumerGroup = (groupId, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.deleteConsumerGroup}/${doubleEncode(groupId)}&clusterName=${activeCluster}`, {method: 'DELETE'})
            .then(statusHandler)
            .then(() => cb())
            .catch(onError)
    }, onError);
};

export const updateTopicConfig = (topic, config, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.updateTopicConfig}?clusterName=${activeCluster}`, {
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
            .catch(onError);
    }, onError);

};

export const getConsumerGroupsForTopic = (topic, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.consumerGroupsForTopic}/${doubleEncode(topic)}&clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result.groups))
            .catch(onError);
    }, onError);

};


export const getConsumerGroupDetailsWithOffsets = (groupdId, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.listConsumerGroupDetailsWithOffsets}/${doubleEncode(groupdId)}&clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result.offsets))
            .catch(onError);
    }, onError);

};

export const getCreateTopicConfig = (cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    addClusterNameParam((activeCluster) => {
        fetch(`${api.createTopicConfig}?clusterName=${activeCluster}`)
            .then(statusHandler)
            .then(res => res.json())
            .then(result => cb(result.configOptions))
            .catch(onError);
    }, onError)

};

export const createTopic = (topicData, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    topicData.requestType = ".CreateTopicRequest";

    addClusterNameParam((activeCluster) => {
        fetch(`${api.createTopic}?clusterName=${activeCluster}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(topicData)
        })
            .then(statusHandler)
            .then(() => cb())
            .catch(onError);
    }, onError);

};

export const deleteTopic = (topic, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);
    addClusterNameParam(activeCluster => {
        fetch(`${api.deleteTopic}/${doubleEncode(topic)}?clusterName=${activeCluster}`, {
            method: 'DELETE',
        })
            .then(statusHandler)
            .then(() => cb())
            .catch(onError);
    }, onError)

};


export const produce = (topic, key, value, headers, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error);

    if (!topic) {
        onError(new Error("Topic must be defined to produce to kafka"));
    } else if (!key) {
        onError(new Error("Key must be defined to produce to kafka"));
    } else {
        addClusterNameParam(activeCluster => {
            fetch(`${api.produce}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    clusterName: activeCluster,
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
                .catch(onError);
        }, onError);
    }
}

export const consume = (topics, limit, fromStart, filters, cb, eb) => {
    let onError = (error) => (eb || errorHandler)(error, "major");

    if (!topics) {
        onError(new Error("Topic must be defined to consume from kafka"));
    } else {

        addClusterNameParam(api.consume, false, (endpoint) => {
            fetch(api.consume, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    clusterName: endpoint,
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
                .catch(onError);
        }, onError)
    }
};

export const consumeToFile = (topics, filters, fileType, columns, separator, eb) => {
    let onError = (error) => (eb || errorHandler)(error, "major");

    if (!topics) {
        onError(new Error("Topic must be defined to consume from kafka"));
    } else {
        addClusterNameParam(api.consumeToFile, false, (endpoint) => {
            let data = JSON.stringify({
                clusterName: endpoint,
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
        });
    }
};