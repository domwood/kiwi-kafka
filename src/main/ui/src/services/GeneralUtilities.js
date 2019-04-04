export const isEmpty = (object) => ! Object.keys(object).some(k => object.hasOwnProperty(k));

export const otherFn = () => true;