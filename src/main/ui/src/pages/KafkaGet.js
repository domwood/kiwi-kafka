import React, {Component} from "react";
import {
    Button,
    ButtonDropdown, ButtonGroup,
    Container,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Form,
    FormGroup,
    Input,
    InputGroup,
    InputGroupAddon,
    Label, Spinner, Table
} from "reactstrap";

import TopicInput from "./../components/TopicInput"
import * as ApiService from "../services/ApiService";
import * as GeneralUtilities from "../services/GeneralUtilities";
import "./../App.css";
import FilterConfigurer from "../components/FilterConfigurer";
import {toast} from "react-toastify";
import WebSocketService from "../services/WebSocketService";

class KafkaGet extends Component {

    constructor(props) {
        super(props);

        this.state = {
            alerts: [],
            bootstrapServers: "",
            targetTopic: "",
            messageLimit: 100,
            messageFromEnd: true,
            messageStartToggle: false,
            messages: [],
            filters: [],
            consumerResponse: null,
            continuous: false,
            consumeCount: 0,
            isReversed: true
        };
    }

    componentWillUnmount() {
        WebSocketService.disconnect();
    }

    setMessageLimit = (messageLimit) => {
        this.setState({messageLimit:messageLimit})
    };

    setTargetTopic = (target) => {
        this.setState({targetTopic:target})
    };

    setMessageFromEnd = (fromEnd) => {
        this.setState({messageFromEnd:fromEnd})
    };

    toggleMessageStartDropdown = () => {
        this.setState({messageStartToggle:!this.state.messageStartToggle})
    };

    setFilter = (filters) => {
        this.setState({
            filters: filters
        });
    };

    getKafkaMessage = () => {
        this.setState({
            consuming: true
        }, () =>{
            ApiService.consume(
                [this.state.targetTopic],
                this.state.messageLimit,
                this.state.messageFromEnd,
                this.state.filters,
                (response) =>{
                    this.setState({
                        consuming: false,
                        messages: response.messages
                    }, () => toast.info(`Retrieved ${response.messages.length} records from ${this.state.targetTopic}`));
                }, (error) => {
                    this.setState({
                        consuming: false
                    });
                    toast.error(`Failed to retrieve data from server ${error.message}`)
                }
            );
        })
    };

    startConsumer = () => {
        this.setState({
            continuous: true,
            messages:[],
            consumeCount:0
        });
        WebSocketService.connect();
        WebSocketService.consume(
            [this.state.targetTopic],
            this.state.filters,
            (response) => {
                this.setState({
                    continuous: true,
                    consumeCount: this.state.consumeCount+response.messages.length,
                    messages: this.state.isReversed ?
                        response.messages.reverse()
                            .concat(this.state.messages)
                            .slice(0, this.state.messageLimit) :
                        this.state.messages
                            .concat(response.messages)
                            .slice(this.state.messages.length + response.messages.length > this.state.messageLimit ?  -this.state.messageLimit : 0)
                });
            },
            (error) => {
                toast.error(`Failed to retrieve data from server ${error.message}`)
                this.setState({
                    continuous: false,
                });
                WebSocketService.disconnect();
            },
            () => {
                this.setState({
                    continuous: false,
                });
                toast.info("Consumer connection closed");
            }
        );
    };

    stopConsumer = () => {
        this.setState({
            continuous: false
        });
        WebSocketService.disconnect();
    };


    render() {
        return (
            <Container className={"WideBoi"}>
                <div className="mt-lg-4"/>
                <h1>Get Data From Kafka</h1>
                <div className="mt-lg-4"/>
                <Form>
                    <TopicInput onUpdate={this.setTargetTopic}/>

                    <div className="mt-lg-1"/>

                    <FormGroup>
                        <Label for="messageLimit">Message Limit</Label>
                        <InputGroup>
                            <Input type="number"
                                   name="messageLimit"
                                   id="messageLimit"
                                   defaultValue={this.state.messageLimit}
                                   onChange={event => this.setMessageLimit(event.target.value)}
                            />
                            <InputGroupAddon addonType="append">
                                <ButtonDropdown direction="down"
                                                isOpen={this.state.messageStartToggle}
                                                toggle={this.toggleMessageStartDropdown}>
                                    <DropdownToggle caret>
                                        {this.state.messageFromEnd ? "From End" : "From Start"}
                                    </DropdownToggle>
                                    <DropdownMenu>
                                        <DropdownItem onClick={() => this.setMessageFromEnd(true)}>Limit From End</DropdownItem>
                                        <DropdownItem onClick={() => this.setMessageFromEnd(false)}>Limit From Start</DropdownItem>
                                    </DropdownMenu>
                                </ButtonDropdown>
                            </InputGroupAddon>
                        </InputGroup>

                        <FilterConfigurer name={"filterConfigurer"} id={"filterConfigurer"} onUpdate={this.setFilter}/>
                    </FormGroup>

                    <ButtonGroup>
                        {
                            !this.state.continuous ?
                                <div>
                                    <Button onClick={this.getKafkaMessage}>Read and Close</Button>
                                    <Button onClick={this.startConsumer}>Consume Continuously</Button>
                                </div>
                                :
                                <div>
                                    <Button onClick={this.getKafkaMessage} disabled={true}>Read and Close</Button>
                                    <Button color="warning" onClick={this.stopConsumer}>Stop Consuming</Button>
                                    <span>Limited to {this.state.messages.length} of {this.state.consumeCount} consumed </span>
                                    <Spinner color="secondary" />
                                </div>
                        }
                        {this.state.consuming ? <Spinner color="secondary" /> : ''}

                    </ButtonGroup>

                    <div className="mt-lg-4"/>
                        <div>
                            {this.state.consumerResponse}
                        </div>
                    <div className="mt-lg-4"/>
                </Form>
                <div>
                {
                    this.state.messages.length > 0 ?
                        <Table size="sm" bordered >
                            <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Partition</th>
                                <th>Offset</th>
                                <th>Key</th>
                                <th>Headers</th>
                                <th>Message</th>
                            </tr>
                            </thead>
                            <tbody className="WrappedTable">
                            {
                                this.state.messages.map(m => {
                                    return (
                                        <tr key={m.partition + "_" + m.offset}>
                                            <td width="10%">{m.timestamp}</td>
                                            <td width="8%">{m.partition}</td>
                                            <td width="6%">{m.offset}</td>
                                            <td width="10%">{m.key}</td>
                                            <td width="20%">{GeneralUtilities.isEmpty(m.headers) ? "" : JSON.stringify(m.headers)}</td>
                                            <td width="46%" >{m.message}</td>
                                        </tr>
                                    )
                                })
                            }
                            </tbody>
                        </Table> : ''
                }
                </div>
            </Container>
        );
    }
}

export default KafkaGet;