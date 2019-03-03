import time from "./TimeProvider";

const register = {};

const handleTime = () => {
    let timestamp = time();
    Object.keys(register).forEach(key => {
        register[key](timestamp);
    });
};

const addToRegister = (key, fn) => {
    register[key] = fn;
};

setInterval(handleTime, 2000);

export default addToRegister;