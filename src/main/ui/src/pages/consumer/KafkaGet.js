import React, {Component} from "react";
import {
    Badge,
    Container,
    Form,
    FormGroup
} from "reactstrap";

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

    toggleActiveTab = (tabId) => {
        this.setState({
            activeTab: tabId
        })
    };

    setTargetTopic = (target) => {
        this.setState({targetTopic:target})
    };

    setMessageLimit = (messageLimit) => {
        this.setState({messageLimit:messageLimit})
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
                <h1>{this.props.isDownload ? 'Download Data From Kafka' : 'Get Data From Kafka'}{this.props.isDownload ? <Badge color="warning">Experimental</Badge> : null}</h1>
                <div className="mt-lg-4"/>
                <Form>
                    <TopicInput onUpdate={this.setTargetTopic} targetTopic={this.state.targetTopic || ''}/>

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
                                />

                                <MessageTable name={"messageTable"}
                                              id={"messageTable"}
                                              messages={this.state.messages} />
                            </div>
                            :
                            <div>
                                <FileDownloader id={'FileDownloader'}
                                                name={'FileDownloader'}
                                                filters={this.state.filters}
                                                targetTopic={this.state.targetTopic}
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
    isDownload: PropTypes.bool.isRequired
};

export default KafkaGet;