export const isEmpty = (object) => ! Object.keys(object).some(k => object.hasOwnProperty(k));

export const prettyArray = (array) => array.length ? array.join(',') : '';