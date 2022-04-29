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
            clusters: [],
            activeCluster: ''
        }
    }

    componentDidMount() {
        this.setState({
            mounted: true
        })
        this.clusterList(this.topicListRefresh);
    }

    componentWillUnmount() {
        this.setState({
            mounted: false
        })
    }

    clusterList = (cb) => {
        ApiService.getKafkaClusterList((clusterList) => {
            let activeCluster = clusterList[0];
            let existingCluster = SessionStore.getActiveCluster();
            if (clusterList.lastIndexOf(existingCluster) > -1) {
                activeCluster = existingCluster;
            }
            this.setClusters(clusterList);
            this.setActiveCluster(activeCluster);
            cb();
        }, () => toast.error("No connection to server"));
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
        if (this.state.mounted) {
            this.setState({
                activeCluster: activeCluster
            });
            SessionStore.setActiveCluster(activeCluster);
        }
    }

    setClusters = (clusters) => {
        if (this.state.mounted) {
            this.setState({
                clusters: clusters
            });
        }
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
                clusters: this.state.clusters,
                activeCluster: this.state.activeCluster,
                setActiveCluster: this.setActiveCluster
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