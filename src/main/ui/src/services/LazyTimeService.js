import time from "./TimeProvider";

const register = {};

const handleTime = () => {
    let timestamp = time();
    Object.keys(register).forEach(key => {
        let remove = register[key](timestamp);
        if(remove) delete register[key];
    });
};

const addToRegister = (key, fn) => {
    register[key] = fn;
};

setInterval(handleTime, 2000);

export default addToRegister;