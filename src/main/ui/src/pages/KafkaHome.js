import React, { Component } from "react";
import {Col, Container, Jumbotron, Row} from "reactstrap";

class KafkaHome extends Component {
    render() {
        return (
            <Jumbotron>
                <Container>
                    <Row>
                        <Col>
                            <h1>Kafka Home</h1>
                        </Col>
                    </Row>
                </Container>
            </Jumbotron>
        );
    }
}

export default KafkaHome;