import React from "react";

export const AppDataContext = React.createContext({
    topicLoading: false,
    topicList: [],
    topicListRefresh: () => {},
    targetTopic: '',
    setTargetTopic: () => {},
    targetTopicValid: false,
    getTopicData: () => {},
    topicData: {}
});
