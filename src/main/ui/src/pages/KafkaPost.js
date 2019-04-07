import React, { Component } from "react";
import {
    Button,
    Form,
    FormGroup,
    Label,
    Input,
    Container,
    InputGroup,
    InputGroupAddon,
    Table
} from 'reactstrap';
import JsonEditor from "./../components/JsonEditor"
import TopicInput from "./../components/TopicInput";

import uuid from "uuid/v4";
import * as ApiService from "../services/ApiService";
import {toast} from "react-toastify";
import "./Pages.css";

class KafkaPost extends Component {

    constructor(props) {
        super(props);

        this.state = {
            bootstrapServers: "",
            targetTopic: "",
            kafkaKey: "",
            kafkaHeaders: {},
            message: "",
            currentKafkaHeaderKey: "",
            currentKafkaHeaderValue: "",
            alerts: [],
            produceResponse: ""
        };
    }

    setRandomKafkaKey = () => {
        this.setState({
            kafkaKey: uuid()
        })
    };

    setTargetTopic = (topic) => {
        this.setState({
            targetTopic: topic
        })
    };

    setKafkaKey = (kafkaKey) => {
        this.setState({
            kafkaKey: kafkaKey
        })
    };

    handleCurrentHeaderKeyChange = (event) => {
        this.setState({
            currentKafkaHeaderKey: event.target.value
        });
    };

    handleCurrentHeaderValueChange = (event) => {
        this.setState({
            currentKafkaHeaderValue: event.target.value
        });
    };

    addHeader = () => {
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
    };

    removeHeader = (key) => {
        let headers = this.state.kafkaHeaders;
        delete headers[key];
        this.setState({
            kafkaHeaders: headers
        })
    };

    updateMessage = (data) => {
        this.setState({
            message: data
        })
    };

    submit = () => {
        ApiService.produce(
            this.state.targetTopic,
            this.state.kafkaKey,
            this.state.message,
            this.state.kafkaHeaders,
            (response) => toast.info(`Produced to ${response.topic} on partition ${response.partition} at offset ${response.offset}`),
            (error) => toast.error(`Failed to produce data: ${error.message}`))
    };

    render() {
        return (
            <Container>


                <div className="mt-lg-4" />
                <h1>Post Data to Kafka</h1>
                <div className="mt-lg-4" />
                <Form>

                    <TopicInput onUpdate={this.setTargetTopic}/>

                    <FormGroup>
                        <Label for="kafkaKey">Kafka Key</Label>
                        <InputGroup>
                            <Input type="text" name="kafkaKey"
                                   id="kafkaKey"
                                   value={this.state.kafkaKey}
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
                            <Table striped className="WrappedTable">
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
                                        <td width={"40%"}>{headerKey}</td>
                                        <td width={"40%"}>{this.state.kafkaHeaders[headerKey]}</td>
                                        <td width={"20%"}><Button onClick={() => this.removeHeader(headerKey)}>Remove Header</Button></td>
                                    </tr>
                                    )
                                })}
                                </tbody>
                            </Table> : ""
                        }

                    </FormGroup>
                    <JsonEditor updateMessage={this.updateMessage} id="kafkaPost" name="kafkaPost"/>

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