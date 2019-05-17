import React, {Component} from "react";
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
import DataStore from "../../services/GlobalStore";
import * as ApiService from "../../services/ApiService";
import * as GeneralUtilities from "../../services/GeneralUtilities";
import {toast} from "react-toastify";
import "../../App.css";
import {MdRefresh} from "react-icons/md";
import CreateTopic from "./components/CreateTopic";
import DeleteTopic from "./components/DeleteTopic";

class KafkaTopics extends Component {

    constructor(props) {
        super(props);

        this.state = {
            topicList: [],
            filteredTopicList: [],
            topicFilter: "",
            loading: false,
            topicData: {},
            refresh: false
        };
    }

    componentDidMount() {
        let topicList = DataStore.get("topicList");
        if (topicList && topicList.length > 0) {
            this.setState({
                topicList: topicList,
                filteredTopicList: topicList
            })
        } else {
            this.reloadTopics();
        }
    }

    reloadTopics = () => {
        this.setState({
            loading: true
        }, () => {
            ApiService.getTopics((topics) => {
                DataStore.put("topicList", topics);
                this.setState({
                    topicList: topics || [],
                    filteredTopicList: topics || [],
                    topicData: topics.reduce((base, topic) => Object.assign(base, {[topic]:null}), {}),
                    loading:false
                }, () => {
                    this.filterTopicList();
                    toast.info("Refreshed topic list from server");
                });
            }, () => {
                this.setState({
                    loading: false
                });
                toast.error("Could not retrieve topic list from server")
            });
        });
    };

    loadDetails = (topic) => {
        if (this.state.topicData[topic]) {
            this.setState({
                topicData: Object.assign(this.state.topicData,
                    Object.assign(this.state.topicData[topic], {toggle: !this.state.topicData[topic].toggle}))
            });
        } else {
            ApiService.getTopicInfo(topic, (details) => {
                Object.assign(details, {
                   toggle: true,
                   view: "partitions"
                });
                this.setState({
                    topicData: Object.assign(this.state.topicData, {[topic]:details})
                });
                toast.info(`Retrieved details for ${topic} topic list from server`)
            }, () => toast.error("Could not retrieve topic list from server"));
        }
    };

    loadTopicData = (topic) => {
        let getGroupOffsets = () => {
            //TODO this is terrible, fix by making API better
            if(this.state.consumers[topic]){
                Object.keys(this.state.consumers[topic]).forEach(groupId => {
                    ApiService.getConsumerGroupOffsetDetails(groupId, offsets => {
                            let consumers = this.state.consumers;
                            Object.entries(offsets).forEach(([topic, groupWithOffsets]) => {
                                Object.entries(groupWithOffsets).forEach(([groupId, offsets]) => {
                                    offsets.forEach(offset => {
                                        ((consumers[topic]||{})[groupId] || [])
                                            .filter(consumerRow => consumerRow.partition === offset.partition)
                                            .map(consumerRow => Object.assign(consumerRow, offset||{}));
                                    })
                                });
                            });
                            this.setState({
                                consumers: consumers
                            })
                        },
                        err => toast.error(`Failed to retrieve consumer offset data ${err.message}`));
                    });
            }
        };

        ApiService.getConsumerGroupTopicDetails(consumers => {
            this.setState({
                consumers: consumers || {}
            }, getGroupOffsets);

            toast.info("Retrieved Consumer Group Details")
        }, (err) => toast.error(`${err.message} Failed to retrieve consumer group details`))
    };

    onTopicViewChange = (topic, viewName) => {
        if (this.state.topicData[topic]) {
            this.setState({
                topicData: Object.assign(this.state.topicData, {[topic]:
                        Object.assign(this.state.topicData[topic], {view: viewName})})
            });
        }

        if (viewName === 'consumers' && (!this.state.consumers || !this.state.consumers[topic])) {
            let consumers = DataStore.get("topicConsumerGroups");
            if (!consumers || consumers.length === 0) {
               this.loadTopicData(topic);
            } else {
                this.setState({
                    consumers: consumers
                });
            }
        }
    };

    filterTopicList = (filter) => {
        if (filter && filter.length > 0) {
            this.setState({
                filteredTopicList: this.state.topicList.filter(topic => topic.toLowerCase().search(filter.toLowerCase()) !== -1),
                topicFilter: filter
            })
        } else {
            this.setState({
                filteredTopicList: this.state.topicList,
                topicFilter: ""
            })
        }
    };

    addTopic = (toggle) => {
        this.setState({
            addTopic: toggle
        })
    };


    //TODO break into components
    render() {
        return (
            <Container className={"WideBoi"}>

                <div className="mt-lg-4"/>
                <h1>Kafka Topics</h1>
                <div className="mt-lg-4"/>
                <div className={"TwoGap"}/>

                <ButtonGroup>
                    <Button color="primary" onClick={this.reloadTopics}>Reload List <MdRefresh/></Button>
                    {!this.state.addTopic ? <Button color="success" onClick={() => this.addTopic(true)}>Add Topic +</Button> : ''}
                    {this.state.loading ? <Spinner color="secondary"/> : ''}
                </ButtonGroup>

                <div className={"Gap"}/>

                {this.state.addTopic ?
                    <div>
                        <CreateTopic onClose={() => this.addTopic(false)} onCreate={this.reloadTopics}/>
                    </div>
                    : ''
                }

                <div className={"Gap"}/>

                <ListGroup>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend">
                                <InputGroupText>Topic Filter:</InputGroupText>
                            </InputGroupAddon>
                            <Input type="text" name="topicSearch" id="topicSearch"
                                   defaultValue=""
                                   onChange={event => this.filterTopicList(event.target.value)}/>
                        </InputGroup>
                    </ListGroupItem>


                    {
                        this.state.filteredTopicList.map(topic => {
                            return (
                                <ListGroupItem key={topic + "_parent"} id={topic}>
                                    <Button size="sm" onClick={() => this.loadDetails(topic)} block>{topic}</Button>
                                    {
                                        this.state.topicData[topic] && this.state.topicData[topic].toggle ? <div>
                                            <ListGroup>
                                                <ListGroupItem key={topic + "_name"}>
                                                    <Label>Name: </Label><b> {this.state.topicData[topic].topic}</b>
                                                </ListGroupItem>
                                                <ListGroupItem key={topic + "_replication"}>
                                                    <Label>Replication
                                                        Count: </Label><b> {this.state.topicData[topic].replicaCount} </b>
                                                </ListGroupItem>
                                                <ListGroupItem key={topic + "_partitions"}>
                                                    <Label>Partitions: </Label><b> {this.state.topicData[topic].partitionCount}</b>
                                                </ListGroupItem>
                                                <ListGroupItem key={topic + "_buttons"}>
                                                    <ButtonGroup>
                                                        <Button color="primary" onClick={() => this.loadTopicData(topic)}>Refresh <MdRefresh/></Button>
                                                        <DeleteTopic topic={topic} onComplete={() => this.reloadTopics()} />
                                                    </ButtonGroup>
                                                </ListGroupItem>
                                                <ListGroupItem key={topic + "_views"}>
                                                    <div className={"Gap"}/>
                                                    <ButtonGroup className={"WideBoiGroup"}>

                                                        <Button
                                                            onClick={() => this.onTopicViewChange(topic, 'partitions')}
                                                            active={this.state.topicData[topic].view === 'partitions'}>
                                                            Partitions
                                                        </Button>
                                                        <Button
                                                            onClick={() => this.onTopicViewChange(topic, 'configuration')}
                                                            active={this.state.topicData[topic].view === 'configuration'}>
                                                            Topic Configuration
                                                        </Button>
                                                        <Button
                                                            onClick={() => this.onTopicViewChange(topic, 'consumers')}
                                                            active={this.state.topicData[topic].view === 'consumers'}>
                                                            Consumer Groups
                                                        </Button>

                                                    </ButtonGroup>
                                                    <div className={"Gap"}/>
                                                    {
                                                        this.state.topicData[topic].view === 'partitions' ?
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
                                                                {this.state.topicData[topic].partitions.map(p => {
                                                                    return (
                                                                        <tr key={topic + "_" + p.partition}>
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
                                                            this.state.topicData[topic].view === 'configuration' ?
                                                                <Table>
                                                                    <thead>
                                                                    <tr>
                                                                        <th>Config Key</th>
                                                                        <th>Config Value</th>
                                                                    </tr>
                                                                    </thead>
                                                                    <tbody>
                                                                    {
                                                                        Object.keys(this.state.topicData[topic].configuration).map(key => {
                                                                            return (
                                                                                <tr key={`${topic}_${key}`}>
                                                                                    <td>{key}</td>
                                                                                    <td>{this.state.topicData[topic].configuration[key]}</td>
                                                                                </tr>
                                                                            )
                                                                        })
                                                                    }
                                                                    </tbody>
                                                                </Table>
                                                                :
                                                                <div>
                                                                    {
                                                                        this.state.consumers && this.state.consumers[topic] ? Object.keys(this.state.consumers[topic]).map(groupId => {
                                                                                return (<Table key={`${topic}_${groupId}_table`}>
                                                                                    <thead>
                                                                                        <tr key={`${topic}_${groupId}_header`}>
                                                                                            <th key={`${topic}_${groupId}_groupId`}>GroupId</th>
                                                                                            <th key={`${topic}_${groupId}_partition`}>Partition</th>
                                                                                            <th key={`${topic}_${groupId}_clientId`}>ClientId</th>
                                                                                            <th key={`${topic}_${groupId}_groupState`}>Group State</th>
                                                                                            <th key={`${topic}_${groupId}_partitionOffset`}>Partition Offset</th>
                                                                                            <th key={`${topic}_${groupId}_groupOffset`}>Consumer Offset</th>
                                                                                            <th key={`${topic}_${groupId}_lag`}>Consumer Lag</th>
                                                                                            <th key={`${topic}_${groupId}_coor`}>Coordinator</th>
                                                                                        </tr>
                                                                                    </thead>
                                                                                    <tbody>
                                                                                    {this.state.consumers[topic][groupId].map(assignment => {
                                                                                        return (
                                                                                            <tr key={`${topic}_${groupId}_${assignment.partition}`}>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_groupId`}>
                                                                                                    {assignment.groupId}
                                                                                                </td>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_part`}>
                                                                                                    {assignment.partition}
                                                                                                </td>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_client`}>
                                                                                                    {assignment.clientId}
                                                                                                </td>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_groupsState`}>
                                                                                                    {assignment.groupState || 'INACTIVE'}
                                                                                                </td>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_partitionOffset`}>
                                                                                                    {assignment.partitionOffset}
                                                                                                </td>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_groupOffset`}>
                                                                                                    {assignment.groupOffset}
                                                                                                </td>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_lag`}>
                                                                                                    {assignment.lag}
                                                                                                </td>
                                                                                                <td key={`${topic}_${groupId}_${assignment.partition}_coor`}>
                                                                                                    {assignment.coordinator}
                                                                                                </td>
                                                                                            </tr>
                                                                                        )
                                                                                    })}
                                                                                    </tbody>
                                                                                </Table>)
                                                                            })
                                                                            : 'No active consumers found.'
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