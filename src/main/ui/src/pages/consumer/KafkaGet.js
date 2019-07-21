import React, {Component} from "react";
import {
    Container,
    Form,
    FormGroup, Nav, NavItem, NavLink, TabContent, TabPane,
} from "reactstrap";

import TopicInput from "../common/TopicInput"
import "../../App.css";
import FilterConfigurer from "./components/FilterConfigurer";
import MessageLimit from "./components/MessageLimit";
import MessageReader from "./components/MessageReader";
import MessageTable from "./components/MessageTable";
import classnames from 'classnames';
import FileDownloader from "./components/FileDownloader";

class KafkaGet extends Component {

    constructor(props) {
        super(props);

        this.state = {
            activeTab: '1',
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
                <h1>Get Data From Kafka</h1>
                <div className="mt-lg-4"/>
                <Form>
                    <TopicInput onUpdate={this.setTargetTopic} targetTopic={this.state.targetTopic || ''}/>

                    <div className="mt-lg-1"/>

                    <FormGroup>
                        <MessageLimit id={"messageLimit"}
                                      name={"messageLimit"}
                                      messageLimit={this.state.messageLimit}
                                      onMessageLimitUpdate={this.setMessageLimit}
                                      />
                        <FilterConfigurer name={"filterConfigurer"} id={"filterConfigurer"} onUpdate={this.setFilter}/>
                    </FormGroup>

                    <Nav tabs>
                        <NavItem>
                            <NavLink
                                className={classnames({ active: this.state.activeTab === '1' })}
                                onClick={() => { this.toggleActiveTab('1'); }}
                            >
                                View Data
                            </NavLink>
                        </NavItem>
                        <NavItem>
                            <NavLink
                                className={classnames({ active: this.state.activeTab === '2' })}
                                onClick={() => { this.toggleActiveTab('2'); }}
                            >
                                Download To File
                            </NavLink>
                        </NavItem>
                    </Nav>

                    <TabContent activeTab={this.state.activeTab}>
                        <TabPane tabId="1">
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

                            <MessageTable name={"messageTable"} id={"messageTable"} messages={this.state.messages} />

                        </TabPane>
                        <TabPane tabId="2">
                            <FileDownloader />
                        </TabPane>
                    </TabContent>

                    <div className="mt-lg-4"/>
                    <div className="mt-lg-4"/>
                </Form>


            </Container>
        );
    }
}

export default KafkaGet;