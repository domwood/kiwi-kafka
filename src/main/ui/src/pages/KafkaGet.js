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
    Label
} from "reactstrap";

import TopicInput from "./../components/TopicInput"

class KafkaGet extends Component {

    constructor(props) {
        super(props);

        this.setTargetTopic = this.setTargetTopic.bind(this);
        this.setMessageLimit = this.setMessageLimit.bind(this);
        this.toggleMessageStartDropdown = this.toggleMessageStartDropdown.bind(this);
        this.setMessageFromEnd = this.setMessageFromEnd.bind(this);

        this.state = {
            alerts: [],
            bootstrapServers: "",
            targetTopic: "",
            messageLimit: 10,
            messageFromEnd: true,
            messageStartToggle: false
        }
    }

    setMessageLimit(messageLimit){
        this.setState({messageLimit:messageLimit})
    }

    setTargetTopic(target){
        this.setState({targetTopic:target})
    }

    setMessageFromEnd(fromEnd){
        this.setState({messageFromEnd:fromEnd})
    }

    toggleMessageStartDropdown(){
        this.setState({messageStartToggle:!this.state.messageStartToggle})
    }


    render() {
        return (
            <Container>
                <div className="mt-lg-4"></div>
                <h1>Get Data From Kafka</h1>
                <div className="mt-lg-4"></div>
                <div>
                    {
                        this.state.alerts.length > 0 ? this.state.alerts.map(a => {
                            return <Alert color="primary">{a.error}</Alert>
                        }) : ""
                    }
                </div>
                <Form>
                    <TopicInput onUpdate={this.setTargetTopic}/>

                    <div className="mt-lg-1"></div>

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

                    <Button onClick={this.submit}>Consume From Kafka</Button>

                    <div className="mt-lg-1"></div>
                </Form>

            </Container>
        );
    }
}

export default KafkaGet;