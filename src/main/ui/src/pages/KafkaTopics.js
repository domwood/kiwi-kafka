import React, { Component } from "react";
import {
    Button,
    ButtonGroup,
    Container,
    Input,
    InputGroup, InputGroupAddon,
    InputGroupText,
    Label,
    ListGroup,
    ListGroupItem, Spinner,
    Table
} from "reactstrap";
import DataStore from "../services/GlobalStore";
import * as ApiService from "../services/ApiService";
import * as GeneralUtilities from "../services/GeneralUtilities";
import {toast} from "react-toastify";
import "./../App.css";
import {MdRefresh} from "react-icons/md";
import CreateTopic from "../components/CreateTopic";

class KafkaTopics extends Component {

    constructor(props) {
        super(props);

        this.state = {
            topicList: [],
            filteredTopicList: [],
            topicFilter: "",
            loading: false
        };
    }

    componentDidMount(){
        let topicList = DataStore.get("topicList");
        if(topicList && topicList.length > 0){
            this.setState({
                topicList: topicList,
                filteredTopicList: topicList
            })
        }
        else{
            this.reloadTopics();
        }
    }

    reloadTopics = () => {
        this.setState({
            loading:true
        }, () => {
            ApiService.getTopics((topics) => {
                DataStore.put("topicList", topics);
                let topicState = this.state;
                topicState.topicList = topics || [];
                topicState.filteredTopicList = topicState.topicList;
                topicState.loading = false;
                topics.forEach(t => topicState[t] = undefined)

                this.setState(topicState, () => {
                    this.filterTopicList();
                    toast.info("Refreshed topic list from server");
                });
            }, () => {
                this.setState({
                    loading:false
                });
                toast.error("Could not retrieve topic list from server")
            });
        });
    };

    loadDetails = (topic) => {
        if(this.state[topic]){
            let topicState = this.state[topic];
            topicState.toggle = !topicState.toggle;
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

        if(viewName === 'consumers' && (!this.state.consumers || !this.state.consumers[topic]) ){
            let consumers = DataStore.get("topicConsumerGroups");
            if(!consumers || consumers.length === 0){
                ApiService.getConsumerGroupTopicDetails(consumers => {
                    this.setState({
                        consumers: consumers
                    });
                    toast.info("Retrieved Consumer Group Details")
                }, (err) => toast.error(`${err.message} Failed to retrieve consumer group details`))
            }
            else{
                this.setState({
                    consumers: consumers
                });
            }
        }
    };

    filterTopicList = (filter) => {
        if(filter && filter.length > 0){
            this.setState({
                filteredTopicList: this.state.topicList.filter(topic => topic.toLowerCase().search(filter.toLowerCase()) !== -1),
                topicFilter: filter
            })
        }
        else{
            this.setState({
                filteredTopicList: this.state.topicList,
                topicFilter: ""
            })
        }
    };

    addTopic = (toggle) => {
        this.setState({
            addTopic:toggle
        })
    };

    render() {
        return (
            <Container className={"WideBoi"}>

                <div className="mt-lg-4" />
                <h1>Kafka Topics</h1>
                <div className="mt-lg-4" />
                <div className={"TwoGap"} />

                {this.state.addTopic ?
                    <div>
                        <CreateTopic onClose={() => this.addTopic(false)} onCreate={this.reloadTopics}/>
                    </div>
                    :
                    <div>
                        <Button onClick={() => this.addTopic(true)}>Add Topic +</Button>
                    </div>
                }

                <div className={"Gap"} />

                <ButtonGroup>
                    <Button outline onClick={this.reloadTopics}>Reload List <MdRefresh /></Button>
                    {this.state.loading ? <Spinner color="secondary" /> : ''}
                </ButtonGroup>

                <div className={"Gap"} />

                <ListGroup>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend">
                                <InputGroupText>Topic Filter:</InputGroupText>
                            </InputGroupAddon>
                            <Input type="text" name="topicSearch" id="topicSearch"
                                   defaultValue=""
                                   onChange={event => this.filterTopicList(event.target.value)} />
                        </InputGroup>
                    </ListGroupItem>


                    {
                        this.state.filteredTopicList.map(topic => {
                            return (
                                <ListGroupItem key={topic+"_parent"} id={topic}>
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
                                                        <div>
                                                            {
                                                                this.state.consumers && this.state.consumers[topic] ? Object.keys(this.state.consumers[topic]).map(groupId =>{
                                                                    return (<Table>
                                                                        <thead>
                                                                        <tr>
                                                                            <th>GroupId</th>
                                                                            <th>Partition</th>
                                                                            <th>ClientId</th>
                                                                            <th>ConsumerId</th>
                                                                        </tr>
                                                                        </thead>
                                                                        <tbody>
                                                                        {this.state.consumers[topic][groupId].map(assignment => {
                                                                            return (<tr>
                                                                                <td>{assignment.groupId}</td>
                                                                                <td>{assignment.partition}</td>
                                                                                <td>{assignment.clientId}</td>
                                                                                <td>{assignment.consumerId}</td>
                                                                            </tr>)
                                                                        })}
                                                                        </tbody>
                                                                    </Table>)
                                                                })
                                                                : ''
                                                            }
                                                        </div>
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