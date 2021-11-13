export const isEmpty = (object) => ! Object.keys(object).some(k => Object.prototype.hasOwnProperty.call(object, k));

export const prettyArray = (array) => array.length ? array.join(',') : '';

export const prettyTimestamp = (timestamp) => {
    try{
        return new Date(timestamp).toISOString();
    }
    catch(err){
        return '??';
    }
};