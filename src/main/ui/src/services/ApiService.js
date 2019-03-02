import api from "./ApiConfig";


export const getTopics = (cb) => {
    fetch(api.listTopics)
        .then(res => res.json())
        .then(result => {
            cb(result.topics);
        });
};

