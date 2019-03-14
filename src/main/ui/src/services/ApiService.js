import api from "./ApiConfig";
import errorHandler from "./ErrorService";

const statusHandler = (response) => {
    if(response.ok){
        return response;
    }
    else{
        console.log(response);
        throw new Error(`${response.status} Error response from Server`);
    }
}

export const getTopics = (cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error, "major");

    fetch(api.listTopics)
        .then(statusHandler)
        .then(res => res.json())
        .then(result => cb(result.topics))
        .catch(errorhandler)
};

export const produce = (topic, key, value, headers, cb, eb) => {
    let errorhandler = (error) => (eb||errorHandler)(error, "major");

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
            .catch(errorhandler)
    }

};