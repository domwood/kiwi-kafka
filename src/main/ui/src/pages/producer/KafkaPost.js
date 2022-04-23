import React, {Component} from "react";
import {Button, ButtonGroup, Container, Form, FormGroup, Input, InputGroup, InputGroupText, Table} from 'reactstrap';
import JsonEditor from "./components/JsonEditor"
import TopicInput from "../common/TopicInput";
import {v4 as uuid} from 'uuid';
import * as ApiService from "../../services/ApiService";
import {toast} from "react-toastify";
import "../../App.css";
import PropTypes from "prop-types";
import ProfileToggleToolTip from "../common/ProfileToggleToolTip";
import {AppDataContext} from "../../contexts/AppDataContext";

class KafkaPost extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);

        this.state = {
            bootstrapServers: "",
            kafkaKey: "",
            kafkaHeaders: [],
            message: null,
            currentKafkaHeaderKey: "",
            currentKafkaHeaderValue: "",
            currentKafkaHeaderMap: "",
            alerts: [],
            produceResponse: "",
            disabledToolTip: false,
            individualHeaders: true
        };
    }

    isPostDisabled = () => {
        let profiles = this.props.profiles || [];
        return profiles.length !== 0 && profiles.indexOf("write-producer") === -1;
    };

    isHeaderActive = () => this.state.currentKafkaHeaderKey || this.state.currentKafkaHeaderValue

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

    setIndividualHeaders = (individualHeaders) => {
        this.setState({
            individualHeaders: individualHeaders
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

    handleHeaderMapChange = (event) => {
        this.setState({
            currentKafkaHeaderMap: event.target.value
        });
    };

    addHeaders = () => {
        try {
            let headerMap = JSON.parse(this.state.currentKafkaHeaderMap)
            let updatedHeaders = this.state.kafkaHeaders
            Object.keys(headerMap).forEach((key) => {
                if (typeof headerMap[key] !== "string") {
                    updatedHeaders.push({key: key, value: JSON.stringify(headerMap[key])})
                } else {
                    updatedHeaders.push({key: key, value: headerMap[key]})
                }
            })
            this.setState({
                currentKafkaHeaderMap: "",
                kafkaHeaders: updatedHeaders
            });
        } catch (e) {
            toast.error(`Not valid format inserted, should be json map ${e.message}`)
        }
    };

    addHeader = () => {
        let headers = this.state.kafkaHeaders;
        let currentKey = this.state.currentKafkaHeaderKey;
        let currentValue = this.state.currentKafkaHeaderValue;
        if (currentKey && currentKey.length > 0) {
            headers.push({key: currentKey, value: currentValue})
            this.setState({
                currentKafkaHeaderKey: "",
                currentKafkaHeaderValue: "",
                kafkaHeaders: headers
            })
        }
    };

    removeHeader = (index) => {
        let headers = this.state.kafkaHeaders;
        headers.splice(index, 1);
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
            this.context.targetTopic,
            this.state.kafkaKey,
            this.state.message,
            this.state.kafkaHeaders,
            (response) => toast.info(`Produced to ${response.topic} on partition ${response.partition} at offset ${response.offset}`),
            (error) => toast.error(`Failed to produce data: ${error.message}`))
    };

    render() {
        return (
            <Container className={"WideBoi"}>


                <div className="mt-lg-4"/>
                <h1>Post Data to Kafka</h1>
                <div className="mt-lg-4"/>
                <Form>

                    <TopicInput/>

                    <FormGroup>
                        <InputGroup>
                            <InputGroupText className="input-group-text-padded">
                                Kafka Key:
                            </InputGroupText>
                            <Input type="text" name="kafkaKey"
                                   id="kafkaKey"
                                   value={this.state.kafkaKey}
                                   onChange={event => this.setKafkaKey(event.target.value)}
                            />
                            <InputGroupText>
                                <Button color="secondary" onClick={this.setRandomKafkaKey}>Random</Button>
                            </InputGroupText>
                        </InputGroup>
                    </FormGroup>
                    <FormGroup>
                        <InputGroup>
                            <InputGroupText className="input-group-text-padded">
                                Kafka Headers:
                            </InputGroupText>
                            <ButtonGroup>
                                <Button outline={!this.state.individualHeaders} color="secondary" size="sm"
                                        onClick={() => this.setIndividualHeaders(true)}>Individual</Button>
                                <Button outline={this.state.individualHeaders} color="secondary" size="sm"
                                        onClick={() => this.setIndividualHeaders(false)}>Map</Button>
                            </ButtonGroup>
                            {
                                this.state.individualHeaders ?
                                    <React.Fragment>
                                        <Input type="text" name="kafkaHeaderKey" id="kafkaHeaderKey"
                                               value={this.state.currentKafkaHeaderKey}
                                               onChange={this.handleCurrentHeaderKeyChange}/>
                                        <Input type="text" name="kafkaHeaderValue" id="kafkaHeaderValue"
                                               value={this.state.currentKafkaHeaderValue}
                                               onChange={this.handleCurrentHeaderValueChange}/>
                                        <InputGroupText>
                                            <Button color={this.isHeaderActive() ? "success" : "secondary"}
                                                    onClick={() => this.addHeader()} disabled={!this.isHeaderActive()}>
                                                Add Header
                                            </Button>
                                        </InputGroupText>
                                    </React.Fragment>
                                    :
                                    <React.Fragment>
                                        <Input type="textarea"
                                               name="kafkaHeadersMap"
                                               id="kafkaHeadersMap"
                                               placeholder='{"someKey":"someValue", "someOtherKey":"someOtherValue"}'
                                               value={this.state.currentKafkaHeaderMap}
                                               onChange={this.handleHeaderMapChange}/>
                                        <InputGroupText>
                                            <Button
                                                style={{"height": "100%"}}
                                                color={this.state.currentKafkaHeaderMap !== "" ? "success" : "secondary"}
                                                onClick={() => this.addHeaders()}
                                                disabled={this.state.currentKafkaHeaderMap === ""}>
                                                Add Headers
                                            </Button>
                                        </InputGroupText>
                                    </React.Fragment>
                            }
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
                                    {this.state.kafkaHeaders.map((header, index) => {
                                        return (
                                            <tr key={header.key + index}>
                                                <td width={"40%"}>{header.key}</td>
                                                <td width={"40%"}>{header.value}</td>
                                                <td width={"20%"}><Button onClick={() => this.removeHeader(index)}
                                                                          style={{width: "100%"}}>Remove
                                                    Header</Button></td>
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

                    <div className="mt-lg-1"/>
                    <Button id="PostButton" onClick={this.submit}
                            disabled={this.isPostDisabled()}>Send!</Button>

                    <ProfileToggleToolTip profiles={this.props.profiles}
                                          id={`kafka_post_write`}
                                          targetProfile={"write-producer"}
                    />

                    <div className="mt-lg-1"/>
                </Form>

            </Container>
        );
    }
}

KafkaPost.propTypes = {
    profiles: PropTypes.array.isRequired
};

export default KafkaPost;