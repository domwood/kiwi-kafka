import React, { Component } from "react";
import {
    Alert,
    Button,
    Container,
    Form
} from "reactstrap";

import TopicInput from "./../components/TopicInput"

class KafkaGet extends Component {

    constructor(props) {
        super(props);

        this.setTargetTopic = this.setTargetTopic.bind(this);

        this.state = {
            alerts: [],
            bootstrapServers: "",
            targetTopic: "",
            dropdownOpen: false
        }
    }

    setTargetTopic(target){
        this.setState({targetTopic:target})
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

                    <Button onClick={this.submit}>Get</Button>

                    <div className="mt-lg-1"></div>
                </Form>

            </Container>
        );
    }
}

export default KafkaGet;