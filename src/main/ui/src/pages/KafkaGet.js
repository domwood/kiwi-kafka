import React, {Component} from "react";
import {
    Alert,
    Button,
    ButtonDropdown,
    Container,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Form,
    FormGroup,
    Input,
    InputGroup,
    InputGroupAddon,
    Label, Row, Table
} from "reactstrap";

import TopicInput from "./../components/TopicInput"
import * as ApiService from "../services/ApiService";
import LazyTimeService from "../services/LazyTimeService";
import * as GeneralUtilities from "../services/GeneralUtilities";
import ButtonGroup from "reactstrap/es/ButtonGroup";
import "./Pages.css";

class KafkaGet extends Component {

    constructor(props) {
        super(props);

        this.state = {
            alerts: [],
            bootstrapServers: "",
            targetTopic: "",
            messageLimit: 10,
            messageFromEnd: true,
            messageStartToggle: false,
            messages: []
        };

        ;
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

    getKafkaMessage = () => {
        let addClear = (count) => LazyTimeService("kafkaGet", () => {
            if(count < 1){
                this.setState({consumerResponse:null});
                return true;
            }
            else {
                count--;
                return false;
            }
        })

        ApiService.consume(
            [this.state.targetTopic],
            this.state.messageLimit,
            this.messageFromEnd,
            (response) =>{
                this.setState({
                    consumerResponse: <Alert color="success">Successfully retrieved {response.messages.length} Records</Alert>,
                    messages: response.messages
                });
                addClear(3);
            }, (error) => {
                this.setState({
                    consumerResponse: <Alert color="danger">{error.message}</Alert>
                });
                addClear(3);
            }
        )
    };


    render() {
        return (
            <Container>
                <div className="mt-lg-4"/>
                <h1>Get Data From Kafka</h1>
                <div className="mt-lg-4"/>
                <div>
                    {
                        this.state.alerts.length > 0 ? this.state.alerts.map(a => {
                            return <Alert color="primary" key={alert.id}>{a.error}</Alert>
                        }) : ""
                    }
                </div>
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
                    </FormGroup>

                    <ButtonGroup>
                        <Button onClick={this.getKafkaMessage}>Consume From Kafka</Button>
                    </ButtonGroup>

                    <div className="mt-lg-4"/>
                        <div>
                            {this.state.consumerResponse}
                        </div>
                    <div className="mt-lg-4"/>
                </Form>
                <Row>
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
                </Row>
            </Container>
        );
    }
}

export default KafkaGet;