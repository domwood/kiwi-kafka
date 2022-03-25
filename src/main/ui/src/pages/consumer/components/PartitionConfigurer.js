import React, {Component} from "react";
import PropTypes from "prop-types";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import {Button, ButtonGroup, ButtonToolbar} from "reactstrap";

class PartitionConfigurer extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            targetTopicPartitions: -1,
            loadPartitions: false,
            unloaded: false
        };
    }

    componentWillUnmount() {
        this.setState({
            unloaded: true
        })
    }

    componentDidMount() {
        this.loadTopic();
    }

    componentDidUpdate(prevProps) {
        if (prevProps.targetTopic !== this.props.targetTopic) {
            this.loadTopic();
        }
    }

    loadTopic = () => {
        if (!this.state.loadPartitions &&
            this.props.targetTopic &&
            this.props.targetTopic !== "" //&&
            /*this.props.topicList.contains(this.props.targetTopic)*/) {
            ApiService.getTopicInfo(this.props.targetTopic, (topicInfo) => {
                if (this.state.unloaded) return;

                if (this.props.targetTopic === topicInfo.topic) {
                    this.setState({
                        targetTopicPartitions: topicInfo ? topicInfo.partitionCount : -1,
                        loadPartitions: false
                    });
                } else {
                    this.setState({
                        targetTopicPartitions: -1,
                        loadPartitions: true
                    }, this.loadTopic);
                }
            }, () => {
                if (this.state.unloaded) return;
                this.setState({
                    targetTopicPartitions: -1,
                    loadPartitions: false
                });
                toast.error("Could not retrieve partition count for topic " + this.targetTopic + " from server")
            })
        }
    }

    render() {
        return (
            this.state.targetTopicPartitions > -1 ?
                <ButtonToolbar>
                    <ButtonGroup>
                        {Array.from(Array(this.state.targetTopicPartitions).keys())
                            .map(partition => (<Button key={partition}>{partition}</Button>))}
                    </ButtonGroup>
                </ButtonToolbar>
                : <div>{this.props.targetTopic}</div>
        )
    }
}

PartitionConfigurer.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    onUpdate: PropTypes.func.isRequired,
    targetTopic: PropTypes.string.isRequired,
    topicList: PropTypes.array.isRequired
};

export default PartitionConfigurer;