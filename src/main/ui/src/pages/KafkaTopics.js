import React, { Component } from "react";
import {Col, Container, Jumbotron, Row} from "reactstrap";

class KafkaTopics extends Component {
    render() {
        return (
            <Jumbotron>
                <Container>
                    <Row>
                        <Col>
                            <h1>Kafka Topics</h1>
                        </Col>
                    </Row>
                </Container>
            </Jumbotron>
        );
    }
}

export default KafkaTopics;