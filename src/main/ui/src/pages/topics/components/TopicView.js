import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, Label, ListGroup, ListGroupItem, Spinner} from "reactstrap";
import {MdRefresh} from "react-icons/md";
import DeleteTopic from "./DeleteTopic";
import PartitionView from "./PartitionView";
import ConfigurationView from "./ConfigurationView";
import ConsumerView from "./ConsumerView";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";

class TopicView extends Component {
    constructor(props) {
        super(props);

        this.state = {
            topicData: {},
            toggle: false,
            viewName: 'partitions'
        };
    }

    toggleDetails = (refresh) => {
        if (!this.state.toggle || refresh) {
            this.setState({
                loading: true
            }, this.loadDetails)
        } else {
            this.setState({
                toggle: false
            })
        }
    };

    loadDetails = () => {
        ApiService.getTopicInfo(this.props.topic, (details) => {
            this.setState({
                topicData: details,
                toggle: true,
                loading: false
            });
            toast.info(`Retrieved details for ${this.props.topic} topic list from server`)
        }, () => {
            this.setState({loading: false});
            toast.error("Could not retrieve topic list from server")
        });
    }

    onTopicViewChange = (viewName) => {
        this.setState({
            viewName: viewName
        });
    };

    viewSelection = () => {
        if (this.state.viewName === 'partitions') {
            return <PartitionView topic={this.props.topic}
                                  partitions={this.state.topicData.partitions}/>;
        } else if (this.state.viewName === 'configuration') {
            return <ConfigurationView topic={this.props.topic}
                                      configuration={this.state.topicData.configuration}
                                      profiles={this.props.profiles}/>;
        } else {
            return <ConsumerView topic={this.props.topic}/>;
        }
    }

    render() {
        return (
            <ListGroupItem key={this.props.topic + "_parent"} id={this.props.topic}>
                <Button color={this.state.toggle ? "success" : "secondary"} size="sm"
                        onClick={() => this.toggleDetails()} block>{this.props.topic}</Button>
                {this.state.loading ? <Spinner color="secondary"/> : ''}
                {
                    this.state.toggle ?
                        <ListGroup style={{marginTop: "5px"}}>
                            <ListGroupItem>
                                <Label>Name: </Label><b> {this.props.topic}</b>
                            </ListGroupItem>
                            <ListGroupItem>
                                <Label>Replication Count: </Label><b> {this.state.topicData.replicaCount} </b>
                            </ListGroupItem>
                            <ListGroupItem>
                                <Label>Partitions: </Label><b> {this.state.topicData.partitionCount}</b>
                            </ListGroupItem>
                            <ListGroupItem>
                                <ButtonGroup>
                                    <Button color="primary"
                                            onClick={() => this.toggleDetails(true)}>Refresh <MdRefresh/></Button>
                                    <DeleteTopic topic={this.props.topic} onComplete={this.props.onDeletion}
                                                 profiles={this.props.profiles}/>
                                </ButtonGroup>
                            </ListGroupItem>
                            <ListGroupItem>
                                <div className={"Gap"}/>
                                <ButtonGroup className={"WideBoiGroup"}>
                                    <Button
                                        onClick={() => this.onTopicViewChange('partitions')}
                                        color={this.state.viewName === 'partitions' ? 'success' : 'secondary'}>
                                        Partitions
                                    </Button>
                                    <Button
                                        onClick={() => this.onTopicViewChange('configuration')}
                                        color={this.state.viewName === 'configuration' ? 'success' : 'secondary'}>
                                        Topic Configuration
                                    </Button>
                                    <Button
                                        onClick={() => this.onTopicViewChange('consumers')}
                                        color={this.state.viewName === 'consumers' ? 'success' : 'secondary'}>
                                        Active Consumer Groups
                                    </Button>
                                </ButtonGroup>
                                <div className={"Gap"}/>
                                {
                                    this.viewSelection()
                                }

                            </ListGroupItem>
                        </ListGroup>
                        : <React.Fragment/>
                }

            </ListGroupItem>
        )
    }
}

TopicView.propTypes = {
    topic: PropTypes.string.isRequired,
    profiles: PropTypes.array.isRequired,
    onDeletion: PropTypes.func.isRequired
};

export default TopicView;