import React, { Component } from "react";
import {
    Alert,
    Button,
    Form,
    FormGroup,
    Label,
    Input,
    Container,
    InputGroup,
    InputGroupAddon,
    InputGroupButtonDropdown,
    DropdownToggle,
    DropdownMenu,
    DropdownItem,
    Table
} from 'reactstrap';
import JsonEditor from "./../components/JsonEditor"

import uuid from "uuid/v4";
import * as ApiService from "../services/ApiService";

class KafkaPost extends Component {

    constructor(props) {
        super(props);

        this.addAlert = this.addAlert.bind(this);
        this.removeAlert = this.removeAlert.bind(this);
        this.setTargetTopic = this.setTargetTopic.bind(this);
        this.toggleDropDown = this.toggleDropDown.bind(this);
        this.getTopicList = this.getTopicList.bind(this);
        this.setKafkaKey = this.setKafkaKey.bind(this);
        this.setRandomKafkaKey = this.setRandomKafkaKey.bind(this);
        this.addHeader = this.addHeader.bind(this);
        this.removeHeader = this.removeHeader.bind(this);
        this.handleCurrentHeaderKeyChange = this.handleCurrentHeaderKeyChange.bind(this);
        this.handleCurrentHeaderValueChange = this.handleCurrentHeaderValueChange.bind(this);
        this.updateMessage = this.updateMessage.bind(this);
        this.submit = this.submit.bind(this);

        this.state = {
            bootstrapServers: "",
            targetTopic: "",
            kafkaKey: "",
            kafkaHeaders: {},
            message: "",
            topicList: [],
            toggleDropDown: false,
            dropdownOpen: false,
            currentKafkaHeaderKey: "",
            currentKafkaHeaderValue: "",
            alerts: [],
            produceResponse: ""
        };
    }

    addAlert(alert){
        let id = uuid();
        let alerts = this.state.alerts;
        alert.id = id;
        alerts.push(alert);
        this.state.setState({
            alerts: alerts
        });
    }

    removeAlert(alertId){
        let alertList = this.state.alerts.filter(a => a.id !== alertId);
        this.setState({
            alerts: alertList
        });
    }

    getTopicList() {
        if(this.state.topicList.length === 0){
            ApiService.getTopics((topics) => {
                this.setState({
                    topicList:topics
                });
            });
        }
    }

    toggleDropDown() {
        this.getTopicList();
        this.setState({
            dropdownOpen: !this.state.dropdownOpen
        });
    }

    setRandomKafkaKey(){
        this.setState({
            kafkaKey: uuid()
        })
    }

    setTargetTopic(topic){
        this.setState({
            targetTopic: topic
        })
    }

    setKafkaKey(kafkaKey){
        this.setState({
            kafkaKey: kafkaKey
        })
    }

    handleCurrentHeaderKeyChange(event){
        this.setState({
            currentKafkaHeaderKey: event.target.value
        });
    }

    handleCurrentHeaderValueChange(event){
        this.setState({
            currentKafkaHeaderValue: event.target.value
        });
    }

    addHeader(){
        let headers = this.state.kafkaHeaders;
        let currentKey = this.state.currentKafkaHeaderKey;
        let currentValue = this.state.currentKafkaHeaderValue;
        if(currentKey && currentKey.length > 0){
            headers[currentKey] = currentValue;
            this.setState({
                currentKafkaHeaderKey: "",
                currentKafkaHeaderValue: "",
                kafkaHeaders: headers
            })
        }
    }

    removeHeader(key){
        let headers = this.state.kafkaHeaders;
        delete headers[key];
        this.setState({
            kafkaHeaders: headers
        })
    }

    updateMessage(data){
        this.setState({
            message: data
        })
    }

    submit(){
        ApiService.produce(
            this.state.targetTopic,
            this.state.kafkaKey,
            this.state.message,
            this.state.kafkaHeaders,
            (response) =>{
                let msg = `Produced to Topic ${response.topic} on partition ${response.partition} at offset ${response.offset}`
                this.setState({
                    produceResponse: <Alert color="success">{msg}</Alert>
                });
            }, (error) => {
                this.setState({
                    produceResponse: <Alert color="danger">{error.message}</Alert>
                });
            })
    }

    render() {
        return (
            <Container>
                <div className="mt-lg-4"></div>
                <h1>Post Data to Kafka</h1>
                <div className="mt-lg-4"></div>
                <div>
                    {
                        this.state.alerts.length > 0 ? this.state.alerts.map(a => {
                            return <Alert color="primary">{a.error}</Alert>
                        }) : ""
                    }
                </div>
                <Form>
                    <FormGroup>
                        <Label for="topic">Topic:</Label>

                        <InputGroup>
                            <Input type="text" name="topic" id="topic"
                                   defaultValue={this.state.targetTopic}
                                   onChange={event => this.setTargetTopic(event.target.value)} />

                            <InputGroupButtonDropdown addonType="append" isOpen={this.state.dropdownOpen} toggle={this.toggleDropDown}>

                                <DropdownToggle caret>
                                    Topic List
                                </DropdownToggle>
                                <DropdownMenu>{
                                    this.state.topicList.map(topic =>
                                        <DropdownItem key={topic} name={topic} onClick={() => this.setTargetTopic(topic)}>{topic}</DropdownItem>)
                                }</DropdownMenu>
                            </InputGroupButtonDropdown>
                        </InputGroup>
                    </FormGroup>
                    <FormGroup>
                        <Label for="kafkaKey">Kafka Key</Label>
                        <InputGroup>
                            <Input type="text" name="kafkaKey"
                                   id="kafkaKey"
                                   defaultValue={this.state.kafkaKey}
                                   onChange={event => this.setKafkaKey(event.target.value)}
                                    />
                            <InputGroupAddon addonType="append">
                                <Button color="secondary" onClick={this.setRandomKafkaKey}>Random</Button>
                            </InputGroupAddon>
                        </InputGroup>
                    </FormGroup>
                    <FormGroup>
                        <Label for="kafkaHeaders">Kafka Headers</Label>
                        <InputGroup>
                            <Input type="text" name="kafkaHeaderKey" id="kafkaHeaderKey"
                                   value={this.state.currentKafkaHeaderKey} onChange={this.handleCurrentHeaderKeyChange}/>
                            <Input type="text" name="kafkaHeaderValue" id="kafkaHeaderValue"
                                   value={this.state.currentKafkaHeaderValue} onChange={this.handleCurrentHeaderValueChange}/>
                            <InputGroupAddon addonType="append">
                                <Button color="secondary" onClick={() => this.addHeader()}>Add Header</Button>
                            </InputGroupAddon>
                        </InputGroup>
                        {
                            Object.keys(this.state.kafkaHeaders).length > 0 ?
                            <Table striped>
                                <thead>
                                <tr>
                                    <th>Header</th>
                                    <th>Value</th>
                                    <th>#</th>
                                </tr>
                                </thead>
                                <tbody>
                                {Object.keys(this.state.kafkaHeaders).map(headerKey => {
                                    return (
                                    <tr key={headerKey}>
                                        <td>{headerKey}</td>
                                        <td>{this.state.kafkaHeaders[headerKey]}</td>
                                        <td><Button color="primary" onClick={() => this.removeHeader(headerKey)}>Remove Header</Button></td>
                                    </tr>
                                    )
                                })}
                                </tbody>
                            </Table> : ""
                        }

                    </FormGroup>
                    <JsonEditor addAlert={this.addAlert} updateMessage={this.updateMessage} id="kafkaPost" name="kafkaPost"/>

                    <div>
                        {this.state.produceResponse}
                    </div>

                    <div className="mt-lg-1"></div>

                    <Button onClick={this.submit}>Send!</Button>

                    <div className="mt-lg-1"></div>
                </Form>

            </Container>
        );
    }
}

export default KafkaPost;