import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, Label, ListGroup, ListGroupItem, Spinner, Table} from "reactstrap";
import {MdRefresh} from "react-icons/md";
import DeleteTopic from "./DeleteTopic";
import PartitionView from "./PartitionView";
import ConfigurationView from "./ConfigurationView";
import ConsumerView from "./ConsumerView";
import {AppDataContext} from "../../../contexts/AppDataContext";

class TopicView extends Component {

    static contextType = AppDataContext

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
            }, () => this.loadDetails(refresh))
        } else {
            this.setState({
                toggle: false
            })
        }
    };

    loadDetails = (forceRefresh) => {
        this.context.getTopicData(this.props.topic, forceRefresh, (details) => {
            this.setState({
                topicData: details,
                toggle: true,
                loading: false
            });
        }, () => {
            this.setState({loading: false});
        });
    }

    onTopicViewChange = (viewName) => {
        this.setState({
            viewName: viewName
        });
    };

    viewSelection = () => {
        if (this.state.viewName === 'partitions') {
            return <React.Fragment>
                <Table>
                    <tbody>
                    <tr style={{textAlign: "center", width: "100%"}}>
                        <td style={{width: "25%", paddingTop: "15px"}}>
                            <Label>Name: </Label><b> {this.props.topic}</b>
                        </td>
                        <td style={{width: "25%", paddingTop: "15px"}}>
                            <Label>Replication Count: </Label><b> {this.state.topicData.replicaCount} </b>
                        </td>
                        <td style={{width: "25%", paddingTop: "15px"}}>
                            <Label>Partitions: </Label><b> {this.state.topicData.partitionCount}</b>
                        </td>
                        <td style={{width: "25%"}}>
                            <ButtonGroup>
                                <Button color="primary"
                                        onClick={() => this.toggleDetails(true)}>Refresh Topic
                                    Details<MdRefresh/></Button>
                                <DeleteTopic topic={this.props.topic}
                                             onComplete={this.props.onDeletion}
                                             profiles={this.props.profiles}/>
                            </ButtonGroup>
                        </td>
                    </tr>
                    </tbody>
                </Table>
                <PartitionView topic={this.props.topic}
                               partitions={this.state.topicData.partitions}/>
            </React.Fragment>
        } else if (this.state.viewName === 'configuration') {
            return <ConfigurationView topic={this.props.topic}
                                      configuration={this.state.topicData.configuration}
                                      profiles={this.props.profiles}/>
        } else {
            return <ConsumerView topic={this.props.topic} profiles={this.props.profiles}/>
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
                        <ListGroup style={{
                            marginTop: "-30px",
                            paddingTop: "30px",
                            marginBottom: "15px",
                            boxShadow: "0 5px 10px slategray",
                            borderRadius: "0"
                        }}>
                            <ListGroupItem style={{paddingTop: "0"}}>
                                <ButtonGroup className={"WideBoiGroup"}>
                                    <Button
                                        onClick={() => this.onTopicViewChange('partitions')}
                                        color={this.state.viewName === 'partitions' ? 'success' : 'secondary'}>
                                        Topic Info & Partitions
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