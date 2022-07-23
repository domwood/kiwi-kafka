import React from "react";

const NOOP = () => {};
export const CLOSED_STATE = "CLOSED";
export const CONSUMING_STATE = "CONSUMING";
export const PAUSED_STATE = "PAUSING";

export const AppDataContext = React.createContext({
    topicLoading: false,
    topicList: [],
    topicListRefresh: NOOP,
    targetTopic: '',
    setTargetTopic: NOOP,
    targetTopicValid: false,
    getTopicData: NOOP,
    topicData: {},
    consumingState: CLOSED_STATE,
    setConsumingState: NOOP,
    setActiveCluster: NOOP,
    getActiveCluster: NOOP,
    clearState: NOOP
});
