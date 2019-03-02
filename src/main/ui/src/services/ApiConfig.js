
export default ((process||{}).env||{}).NODE_ENV === 'LOCAL' ? process.env.LOCAL_SPRING_API : 'api';