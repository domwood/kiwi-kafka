import React, {Component} from "react";
import {Container, Form, FormGroup} from "reactstrap";

import TopicInput from "../common/TopicInput"
import "../../App.css";
import FilterConfigurer from "./components/FilterConfigurer";
import MessageLimit from "./components/MessageLimit";
import MessageReader from "./components/MessageReader";
import MessageTable from "./components/MessageTable";
import FileDownloader from "./components/FileDownloader";
import PropTypes from "prop-types";

class KafkaGet extends Component {

    constructor(props) {
        super(props);

        this.state = {
            alerts: [],
            bootstrapServers: "",
            targetTopic: "",
            messageLimit: 1000,
            messageFromEnd: true,
            messages: [],
            filters: [],
            continuous: false,
            consumeCount: 0,
            isReversed: true
        };
    }

    componentDidMount() {
        this.mounted = true;
    }

    componentWillUnmount() {
        this.mounted = false;
    }

    setTargetTopic = (target) => {
        this.setState({targetTopic: target})
    };

    setMessageLimit = (messageLimit) => {
        this.setState({messageLimit: messageLimit})
    };

    setFilter = (filters) => {
        this.setState({
            filters: filters
        });
    };

    setMessages = (msgs) => {
        if (this.mounted) {
            this.setState({
                messages: msgs
            });
        }
    };

    render() {
        return (
            <Container className={"WideBoi"}>
                <div className="mt-lg-4"/>
                <h1 id={"kafkaGetDataTitle"}>{this.props.isDownload ? "Download Data From Kafka" : "Get Data From Kafka"}</h1>
                <div className="mt-lg-4"/>
                <Form>
                    <TopicInput onUpdate={this.setTargetTopic} targetTopic={this.state.targetTopic || ""}/>

                    <div className="mt-lg-1"/>

                    <FormGroup>
                        {!this.props.isDownload ?
                            <MessageLimit id={"messageLimit"}
                                          name={"messageLimit"}
                                          messageLimit={this.state.messageLimit}
                                          onMessageLimitUpdate={this.setMessageLimit}
                            />
                            : null
                        }
                        <FilterConfigurer name={"filterConfigurer"} id={"filterConfigurer"} onUpdate={this.setFilter}/>
                    </FormGroup>

                    {
                        !this.props.isDownload ?
                            <div>
                                <MessageReader
                                    name={"messageReader"} id={"messageReader"}
                                    filters={this.state.filters}
                                    targetTopic={this.state.targetTopic || ''}
                                    messageLimit={this.state.messageLimit}
                                    messageFromEnd={false}
                                    isReversed={true}
                                    updateMessages={this.setMessages}
                                    messages={this.state.messages}
                                    profiles={this.props.profiles}
                                />

                                <MessageTable name={"messageTable"}
                                              id={"messageTable"}
                                              messages={this.state.messages}/>
                            </div>
                            :
                            <div>
                                <FileDownloader id={"FileDownloader"}
                                                name={"FileDownloader"}
                                                filters={this.state.filters}
                                                targetTopic={this.state.targetTopic}
                                                profiles={this.props.profiles}
                                />
                            </div>
                    }

                    <div className="mt-lg-4"/>
                    <div className="mt-lg-4"/>
                </Form>


            </Container>
        );
    }
}

KafkaGet.propTypes = {
    isDownload: PropTypes.bool.isRequired,
    profiles: PropTypes.array.isRequired
};

export default KafkaGet;