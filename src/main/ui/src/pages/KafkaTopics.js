import React, { Component } from "react";
import {Button, ButtonGroup, Container, Label, ListGroup, ListGroupItem, Table} from "reactstrap";
import DataStore from "../services/GlobalStore";
import * as ApiService from "../services/ApiService";
import * as GeneralUtilities from "../services/GeneralUtilities";
import {toast} from "react-toastify";
import "./Pages.css";
import {MdRefresh} from "react-icons/md";

class KafkaTopics extends Component {

    constructor(props) {
        super(props);

        this.state = {
            topicList: DataStore.get("topicList") || []
        };

        if(this.state.topicList.length === 0){
            ApiService.getTopics((topics) => {
                DataStore.put("topicList", topics);
                this.setState({
                    topicList: topics || []
                });
            }, () => toast.warn("Could not retrieve topic list from server"));
        }
    }

    reloadTopics = () => {
        ApiService.getTopics((topics) => {
            DataStore.put("topicList", topics);
            this.setState({
                topicList: topics || []
            });
            toast.info("Refreshed topic list from server")
        }, () => toast.error("Could not retrieve topic list from server"));
    };

    loadDetails = (topic) => {
        if(this.state[topic] && this.state[topic].toggle){
            let topicState = this.state[topic];
            topicState.toggle = false;
            this.setState({
                [topic]: topicState
            });
        }
        else{
            ApiService.getTopicInfo(topic, (details) => {
                details.toggle = true;
                details.view = "partitions";
                this.setState({
                    [topic]: details
                });
                toast.info(`Retrieved details for ${topic} topic list from server`)
            }, () => toast.error("Could not retrieve topic list from server"));
        }
    };

    onTopicViewChange = (topic, viewName) => {
        if(this.state[topic]){
            let topicState = this.state[topic];
            topicState.view = viewName;
            this.setState({
                [topic]: topicState
            });
        }
    };

    render() {
        return (
            <Container className={"WideBoi"}>

                <div className="mt-lg-4" />
                <h1>Kafka Topics</h1>
                <div className="mt-lg-4" />
                <div className={"Gap"} />

                <Button outline onClick={this.reloadTopics}>Reload List <MdRefresh /></Button>
                <div className={"Gap"} />
                <div className={"Gap"} />
                <div className={"Gap"} />

                <ListGroup>
                    {
                        this.state.topicList.map(topic => {
                            return (
                                <ListGroupItem key={topic+"_parent"}>
                                    <Button size="sm" onClick={() => this.loadDetails(topic)} block>{topic}</Button>
                                    {
                                        this.state[topic] && this.state[topic].toggle ? <div>
                                            <ListGroup>
                                                <ListGroupItem key={topic+"_name"}>
                                                    <Label>Name: </Label><b> {this.state[topic].topic}</b>
                                                </ListGroupItem>
                                                <ListGroupItem key={topic+"_replication"}>
                                                    <Label>Replication Count: </Label><b> {this.state[topic].replicaCount} </b>
                                                </ListGroupItem>
                                                <ListGroupItem key={topic+"_partitions"}>
                                                    <Label>Partitions: </Label><b> {this.state[topic].partitionCount}</b>
                                                </ListGroupItem>
                                                <ListGroupItem key={topic+"views"}>
                                                    <div className={"Gap"}></div>
                                                    <ButtonGroup className={"WideBoiGroup"}>

                                                        <Button onClick={() => this.onTopicViewChange(topic, 'partitions')} active={this.state[topic].view === 'partitions'}>
                                                            Partitions
                                                        </Button>
                                                        <Button onClick={() => this.onTopicViewChange(topic, 'configuration')} active={this.state[topic].view === 'configuration'}>
                                                            Topic Configuration
                                                        </Button>
                                                        <Button onClick={() => this.onTopicViewChange(topic, 'consumers')} active={this.state[topic].view === 'consumers'}>
                                                            Consumer Groups
                                                        </Button>

                                                    </ButtonGroup>
                                                    <div className={"Gap"}></div>
                                                    {
                                                        this.state[topic].view === 'partitions' ?
                                                        <Table>
                                                            <thead>
                                                                <tr>
                                                                    <th>Number</th>
                                                                    <th>Replication Count</th>
                                                                    <th>Replica Nodes</th>
                                                                    <th>ISRs</th>
                                                                    <th>Leader</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                            {this.state[topic].partitions.map(p => {
                                                                return (
                                                                    <tr key={topic +"_" +p.partition}>
                                                                        <td>{p.partition}</td>
                                                                        <td>{p.replicationfactor}</td>
                                                                        <td>{GeneralUtilities.prettyArray(p.replicas)}</td>
                                                                        <td>{GeneralUtilities.prettyArray(p.isrs)}</td>
                                                                        <td>{p.leader}</td>
                                                                    </tr>
                                                                )
                                                            })}
                                                            </tbody>
                                                        </Table> :
                                                        this.state[topic].view === 'configuration' ?
                                                        <Table>
                                                            <thead>
                                                                <tr>
                                                                    <th>Config Key</th>
                                                                    <th>Config Value</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                            {
                                                                Object.keys(this.state[topic].configuration).map(key => {
                                                                    return (
                                                                        <tr key={topic+"_"+key}>
                                                                            <td>{key}</td>
                                                                            <td>{this.state[topic].configuration[key]}</td>
                                                                        </tr>
                                                                    )
                                                                })
                                                            }
                                                            </tbody>
                                                        </Table>
                                                            :
                                                        <div>TODO Consumer Groups</div>
                                                    }

                                                </ListGroupItem>
                                            </ListGroup>

                                        </div> : ''
                                    }
                                </ListGroupItem>
                            )
                        })
                    }
                </ListGroup>
            </Container>
        );
    }
}

export default KafkaTopics;