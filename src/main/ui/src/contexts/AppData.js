import React, {Component} from "react";
import PropTypes from "prop-types";
import {AppDataContext, CLOSED_STATE} from "./AppDataContext";
import * as ApiService from "../services/ApiService";
import {toast} from "react-toastify";
import SessionStore from "../services/SessionStore";

class AppData extends Component {
    constructor(props) {
        super(props);

        this.state = {
            topicList: [],
            topicLoading: false,
            targetTopic: '',
            targetTopicValid: false,
            topicData: {},
            mounted: false,
            consumingState: CLOSED_STATE,
        }
    }

    clearState = () => {
        this.setState({
            topicList: [],
            topicLoading: false,
            targetTopic: '',
            targetTopicValid: false,
            topicData: {}
        }, () => {
            this.topicListRefresh();
        });
    }

    componentDidMount() {
        this.setState({
            mounted: true
        })
        this.topicListRefresh();
    }

    componentWillUnmount() {
        this.setState({
            mounted: false
        })
    }

    topicListRefresh = () => {
        let updateTopicList = (topics, error) => {
            if (this.state.mounted) {
                this.setState({
                    topicLoading: false,
                    topicList: topics
                });
            }
            if (error) {
                toast.error("Could not retrieve topic list from server");
            }
        }

        this.setState({
            topicLoading: true
        }, () => {
            ApiService.getTopics((topics) => {
                updateTopicList(topics, false);
            }, () => {
                updateTopicList([], true);
            })
        })
    }

    setTargetTopic = (targetTopic) => {
        let originalTopicInfo = this.state.targetTopic;
        this.setState({
            targetTopic: targetTopic,
            targetTopicValid: this.state.topicList.indexOf(targetTopic) !== -1
        }, () => {
            if (this.state.targetTopic !== originalTopicInfo && this.state.targetTopicValid) {
                this.getTopicData(targetTopic, false, () => {})
            }
        });
    }

    getTopicData = (targetTopic, forceRefresh, topicInfoCb) => {
        if (this.state.topicData[targetTopic] && !forceRefresh) {
            topicInfoCb(this.state.topicData[targetTopic]);
        } else {
            ApiService.getTopicInfo(targetTopic, (topicInfo) => {
                let topicData = this.state.topicData;
                topicData[targetTopic] = topicInfo
                this.setState({
                    topicData: topicData
                }, () => topicInfoCb(topicInfo));
            }, () => {
                toast.error("Could not retrieve partition count for topic " + this.context.targetTopic + " from server")
                topicInfoCb({});
            })
        }
    }

    setConsumingState = (newState) => {
        if (this.state.mounted) {
            this.setState({
                consumingState: newState
            });
        }
    }

    setActiveCluster = (activeCluster) => {
        SessionStore.setActiveCluster(activeCluster);
    }

    getActiveCluster = (cb, eb) => {
        SessionStore.getActiveCluster(cb, eb);
    }

    render() {
        return (
            <AppDataContext.Provider value={{
                topicListRefresh: this.topicListRefresh,
                topicList: this.state.topicList,
                topicLoading: this.state.topicLoading,
                targetTopic: this.state.targetTopic,
                setTargetTopic: this.setTargetTopic,
                targetTopicValid: this.state.targetTopicValid,
                getTopicData: this.getTopicData,
                topicData: this.state.topicData,
                consumingState: this.state.consumingState,
                setConsumingState: this.setConsumingState,
                setActiveCluster: this.setActiveCluster,
                getActiveCluster: this.getActiveCluster,
                clearState: this.clearState
            }}>
                {this.props.children}
            </AppDataContext.Provider>
        )
    }
}

AppData.propTypes = {
    children: PropTypes.element
};

export default AppData;