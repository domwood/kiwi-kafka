import timeService from './LazyTimeService';
import time from "./TimeProvider";

const expireAfterMs = 5000;
let errorStore = [];

timeService('error', (timestamp) => {
    errorStore = errorStore.filter(e => e.time+expireAfterMs > timestamp)
});

const handleError = (error, level) => {
    errorStore.push({
        level: level,
        error: error,
        time: time()
    });

    //TODO convert to a toast or something
    console.error(`${level} ${error}`);
};

export default handleError;