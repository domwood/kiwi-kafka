const dataStore = {};

//TODO use redux or something for this

const put = (key, value) => {
    dataStore[key] = value;
};

const get = (key) => {
    return dataStore[key];
};

const containsKey = (key) => {
    return dataStore[key] !== null || dataStore[key] !== undefined;
};

const remove = (key) => {
    delete dataStore[key];
};

const clear = () => {
    Object.key(dataStore).forEach(key => {
        delete dataStore[key];
    })
};

export default {
    put: put,
    get: get,
    containsKey: containsKey,
    remove: remove,
    clear: clear
};
