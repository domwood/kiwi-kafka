import React, {Component} from "react";
import {
    Container,
    Form,
    FormGroup,
} from "reactstrap";

import TopicInput from "../common/TopicInput"
import "../../App.css";
import FilterConfigurer from "./components/FilterConfigurer";
import MessageLimit from "./components/MessageLimit";
import MessageReader from "./components/MessageReader";
import MessageTable from "./components/MessageTable";

class KafkaGet extends Component {

    constructor(props) {
        super(props);

        this.state = {
            alerts: [],
            bootstrapServers: "",
            targetTopic: "",
            messageLimit: 100,
            messageFromEnd: true,
            messages: [],
            filters: [],
            continuous: false,
            consumeCount: 0,
            isReversed: true
        };
    }

    setTargetTopic = (target) => {
        this.setState({targetTopic:target})
    };

    setMessageLimit = (messageLimit) => {
        this.setState({messageLimit:messageLimit})
    };

    setMessageFromEnd = (fromEnd) => {
        this.setState({messageFromEnd:fromEnd})
    };

    setFilter = (filters) => {
        this.setState({
            filters: filters
        });
    };

    setMessages = (msgs) => {
        this.setState({
            messages: msgs
        })
    };

    render() {
        return (
            <Container className={"WideBoi"}>
                <div className="mt-lg-4"/>
                <h1>Get Data From Kafka</h1>
                <div className="mt-lg-4"/>
                <Form>
                    <TopicInput onUpdate={this.setTargetTopic} targetTopic={this.state.targetTopic}/>

                    <div className="mt-lg-1"/>

                    <FormGroup>
                        <MessageLimit id={"messageLimit"}
                                      name={"messageLimit"}
                                      messageLimit={this.state.messageLimit}
                                      messageFromEnd={this.state.messageFromEnd}
                                      onMessageEndUpdate={this.setMessageFromEnd}
                                      onMessageLimitUpdate={this.setMessageLimit}
                                      />
                        <FilterConfigurer name={"filterConfigurer"} id={"filterConfigurer"} onUpdate={this.setFilter}/>
                    </FormGroup>

                    <MessageReader
                        name={"messageReader"} id={"messageReader"}
                        filters={this.state.filters}
                        targetTopic={this.state.targetTopic}
                        messageLimit={this.state.messageLimit}
                        messageFromEnd={this.state.messageFromEnd}
                        isReversed={true}
                        updateMessages={this.setMessages}
                        messages={this.state.messages}
                        />

                    <div className="mt-lg-4"/>
                    <div className="mt-lg-4"/>
                </Form>
                <MessageTable name={"messageTable"} id={"messageTable"} messages={this.state.messages} />
            </Container>
        );
    }
}

export default KafkaGet;