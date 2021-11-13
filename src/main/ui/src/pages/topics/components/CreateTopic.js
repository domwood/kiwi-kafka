import {
    Button,
    ButtonGroup,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Input,
    InputGroup,
    InputGroupAddon,
    InputGroupText,
    ListGroup,
    ListGroupItem,
    Table
} from "reactstrap";
import React, {Component} from "react";
import PropTypes from "prop-types";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import ProfileToggleToolTip from "../../common/ProfileToggleToolTip";

class CreateTopic extends Component {
    constructor(props) {
        super(props);

        this.state = {
            topicName: "exampleTopic",
            createTopicConfig: [],
            replicationFactor: 3,
            partitions: 10,
            configKey: "",
            configValue: ""
        }
    }

    isCreateDisabled = () => {
        let profiles = this.props.profiles||[];
        return profiles.length !== 0 && profiles.indexOf("write-admin") === -1;
    };

    componentDidMount(){
        ApiService.getCreateTopicConfig(config =>{
            this.setState({
                createTopicConfig: config
            });
        }, () => toast.warn("Could not default create topic configuration from server"))
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
            partitions: count
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

    removeConfig = (configKey) => {
        let topicConfig = this.state.topicConfig || {};
        delete topicConfig[configKey];
        topicConfig = Object.keys(topicConfig).length < 1 ? null : topicConfig;
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
                                   onChange={event => this.setAddTopicName(event.target.value)}
                                   required
                            />
                        </InputGroup>
                    </ListGroupItem>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend">
                                <InputGroupText>Partitions</InputGroupText>
                            </InputGroupAddon>
                            <Input type="number" name="topicAddPartitions" id="topicAddPartitions"
                                   value={this.state.partitions}
                                   onChange={event => this.setAddTopicPartition(event.target.value)}
                                   required
                            />
                        </InputGroup>
                    </ListGroupItem>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend" >
                                <InputGroupText>Replication Factor</InputGroupText>
                            </InputGroupAddon>
                            <Input type="number" name="topicAddReplication" id="topicAddReplication"
                                   value={this.state.replicationFactor}
                                   onChange={event => this.setAddTopicReplication(event.target.value)}
                                   required
                            />
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
                                        this.state.createTopicConfig.map(item => <DropdownItem key={item} onClick={() => this.setConfigKey(item)}>
                                            {item}
                                        </DropdownItem>)
                                    }
                                </DropdownMenu>
                            </Dropdown>

                            <Input type="text" name="configKey" id="configKey"
                                   value={this.state.configKey}
                                   onChange={event => this.setConfigKey(event.target.value)}/>
                            <Input type="text" name="configValue" id="configValue"
                                   value={this.state.configValue}
                                   onChange={event => this.setConfigValue(event.target.value)}/>
                            <InputGroupAddon addonType="append" >
                                {
                                    this.state.configKey && this.state.configValue ?
                                        <Button color="success" onClick={this.addConfig}>Add Configuration</Button> :
                                    this.state.configKey || this.state.configValue ?
                                        <Button color="warning" disabled={true}>Add Configuration</Button> :
                                        <Button color="secondary" disabled={true}>Add Configuration</Button>
                                }

                            </InputGroupAddon>
                        </InputGroup>
                    </ListGroupItem>

                    {
                        this.state.topicConfig ?
                            <ListGroupItem>
                                <Table size="sm">
                                    <thead>
                                    <tr>
                                        <th width="40%">Key</th>
                                        <th width="40%">Value</th>
                                        <th width="20%"></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        Object.keys(this.state.topicConfig).filter(key => this.state.topicConfig[key])
                                            .map(key => <tr key={"config"+key}>
                                                <td>{key}</td>
                                                <td>{this.state.topicConfig[key]}</td>
                                                <td><Button onClick={() => this.removeConfig(key)}>Remove Configuration</Button></td>
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
                                this.state.topicName && !(this.state.configKey || this.state.configValue) ?
                                    <Button id="CreateTopic" color="success" onClick={this.createTopic} disabled={this.isCreateDisabled()}>Create</Button> :
                                    <Button id="CreateTopic" color="secondary" disabled>Create</Button>
                            }
                            <ProfileToggleToolTip profiles={this.props.profiles}
                                                  id={`${this.props.groupId}_create`}
                                                  targetProfile={"write-admin"}
                            />

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
    onCreate: PropTypes.func.isRequired,
    profiles: PropTypes.array.isRequired,
    groupId: PropTypes.string.isRequired
};


export default CreateTopic;