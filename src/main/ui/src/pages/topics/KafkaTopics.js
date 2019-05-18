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
    ListGroupItem, Spinner
} from "reactstrap";
import DataStore from "../../services/GlobalStore";
import * as ApiService from "../../services/ApiService";
import {toast} from "react-toastify";
import "../../App.css";
import {MdRefresh} from "react-icons/md";
import CreateTopic from "./components/CreateTopic";
import DeleteTopic from "./components/DeleteTopic";
import PartitionView from "./components/PartitionView";
import ConfigurationView from "./components/ConfigurationView";
import ConsumerView from "./components/ConsumerView";
import TopicView from "./components/TopicView";

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

    loadDetails = (topic, refresh) => {
        if (this.state.topicData[topic] && !refresh) {
            this.setState({
                topicData: Object.assign(this.state.topicData,
                    Object.assign(this.state.topicData[topic], {toggle: !this.state.topicData[topic].toggle}))
            });
        } else {
            ApiService.getTopicInfo(topic, (details) => {
                Object.assign(details, {
                   toggle: true,
                   view: (this.state.topicData[topic]||{}).view || "partitions"
                });
                this.setState({
                    topicData: Object.assign(this.state.topicData, {[topic]:details})
                });
                toast.info(`Retrieved details for ${topic} topic list from server`)
            }, () => toast.error("Could not retrieve topic list from server"));
        }
    };

    onTopicViewChange = (topic, viewName) => {
        if (this.state.topicData[topic]) {
            this.setState({
                topicData: Object.assign(this.state.topicData, {[topic]:
                        Object.assign(this.state.topicData[topic], {view: viewName})})
            });
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
                        this.state.filteredTopicList.map(topic => <TopicView key={`${topic}_view`} topic={topic} />)
                    }
                </ListGroup>
            </Container>
        );
    }
}

export default KafkaTopics;