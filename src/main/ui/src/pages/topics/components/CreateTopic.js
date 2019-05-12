import {
    Button,
    ButtonGroup, Dropdown, DropdownItem, DropdownMenu, DropdownToggle,
    Input,
    InputGroup,
    InputGroupAddon,
    InputGroupText,
    ListGroup,
    ListGroupItem, Table
} from "reactstrap";
import React, { Component } from "react";
import PropTypes from "prop-types";
import DataStore from "../../../services/GlobalStore";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify/index";

class CreateTopic extends Component {
    constructor(props) {
        super(props);

        this.state = {
            targetTopic: "",
            createTopicConfig: [],
            replicationFactor: 3,
            partitions: 10
        }
    }

    componentDidMount(){
        let createTopicConfig = DataStore.get("createTopicConfig");
        if(createTopicConfig && createTopicConfig.length > 0){
            this.setState({
                createTopicConfig: createTopicConfig
            })
        }
        else{
            ApiService.getCreateTopicConfig(config =>{
                console.log(config);
                this.setState({
                    createTopicConfig: config
                });
                DataStore.put("createTopicConfig", config);
            }, () => toast.warn("Could not default create topic configuration from server"))
        }
    }

    onClose = () => {
        this.props.onClose();
    };

    toggleConfigKeyDropDown = () => {
        this.setState({
            configKeyDropDownOpen: !this.state.configKeyDropDownOpen
        })
    };

    setAddTopicName = (topic) => {
        this.setState({
            topicName: topic
        })
    };

    setAddTopicPartition = (count) => {
        this.setState({
            partitionCount: count
        })
    };

    setAddTopicReplication = (count) => {
        this.setState({
            replicationFactor: count
        })
    };

    setConfigKey = (count) => {
        this.setState({
            configKey: count
        })
    };

    setConfigValue = (count) => {
        this.setState({
            configValue: count
        })
    };

    addConfig = () => {
        let topicConfig = this.state.topicConfig || {};
        if(this.state.configKey && this.state.configValue){
            topicConfig[this.state.configKey] = this.state.configValue;
        }
        this.setState({
            configKey: "",
            configValue: "",
            topicConfig: topicConfig
        })
    };

    createTopic = () => {
        let topic = {
            name: this.state.topicName,
            partitions: this.state.partitions,
            replicationFactor: this.state.replicationFactor,
            configuration: this.state.topicConfig || {}
        };
        ApiService.createTopic(topic, () => {
            toast.info("Successfully created topic " + topic.name);
            this.onClose();
            this.props.onCreate();
        },  (error) => toast.error(`Failed to create topic: ${error.message}`))
    };

    render() {
        return (
            <div>

                <ListGroup>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend">
                                <InputGroupText>Topic Name:</InputGroupText>
                            </InputGroupAddon>
                            <Input type="text" name="topicAddName" id="topicAddName"
                                   value={this.state.topicName}
                                   onChange={event => this.setAddTopicName(event.target.value)} />
                        </InputGroup>
                    </ListGroupItem>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend">
                                <InputGroupText>Partitions</InputGroupText>
                            </InputGroupAddon>
                            <Input type="number" name="topicAddPartitions" id="topicAddPartitions"
                                   value={this.state.partitions}
                                   onChange={event => this.setAddTopicPartition(event.target.value)} />
                        </InputGroup>
                    </ListGroupItem>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend" >
                                <InputGroupText>Replication Factor</InputGroupText>
                            </InputGroupAddon>
                            <Input type="number" name="topicAddReplication" id="topicAddReplication"
                                   value={this.state.replicationFactor}
                                   onChange={event => this.setAddTopicReplication(event.target.value)} />
                        </InputGroup>
                    </ListGroupItem>


                    <ListGroupItem>
                        <InputGroup>
                            <Dropdown isOpen={this.state.configKeyDropDownOpen} toggle={this.toggleConfigKeyDropDown}>
                                <DropdownToggle caret outline>
                                    Config Option
                                </DropdownToggle>
                                <DropdownMenu>
                                    {
                                        this.state.createTopicConfig.map(item => <DropdownItem onClick={() => this.setConfigKey(item)}>
                                            {item}
                                        </DropdownItem>)
                                    }
                                </DropdownMenu>
                            </Dropdown>

                            <Input type="text" name="configKey" id="configKey"
                                   value={this.state.configKey}
                                   onChange={event => this.setConfigKey(event.target.value)} />
                            <Input type="text" name="configValue" id="configValue"
                                   value={this.state.configValue}
                                   onChange={event => this.setConfigValue(event.target.value)} />
                            <InputGroupAddon addonType="append" >
                                {
                                    this.state.configKey && this.state.configValue ?
                                        <Button color="success" onClick={this.addConfig}>Add Configuration</Button> :
                                        <Button color="secondary" disabled={true}>Add Configuration</Button>
                                }

                            </InputGroupAddon>
                        </InputGroup>
                    </ListGroupItem>

                    {
                        this.state.topicConfig ?
                            <ListGroupItem>
                                <Table sm>
                                    <thead>
                                    <tr>
                                        <th>Key</th>
                                        <th>Value</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        Object.keys(this.state.topicConfig).filter(key => this.state.topicConfig[key])
                                            .map(key => <tr key={"config"+key}>
                                                <td>{key}</td>
                                                <td>{this.state.topicConfig[key]}</td>
                                            </tr>)
                                    }
                                    </tbody>
                                </Table>
                            </ListGroupItem>
                            : ''
                    }


                    <ListGroupItem>
                        <ButtonGroup>
                            {
                                this.state.topicName ?
                                    <Button color="success" onClick={this.createTopic}>Create</Button> :
                                    <Button color="secondary" disabled>Create</Button>
                            }

                            <Button onClick={this.onClose}>Cancel</Button>
                        </ButtonGroup>
                    </ListGroupItem>

                </ListGroup>

            </div>
        )
    }

}

CreateTopic.propTypes = {
    onClose: PropTypes.func.isRequired,
};


export default CreateTopic;