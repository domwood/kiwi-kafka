import {
    Button,
    ButtonGroup,
    Input,
    InputGroup,
    InputGroupAddon,
    InputGroupText,
    ListGroup,
    ListGroupItem
} from "reactstrap";
import React, { Component } from "react";
import PropTypes from "prop-types";

class CreateTopic extends Component {
    constructor(props) {
        super(props);

        this.state = {
            targetTopic: ""
        }
    }

    onClose = () => {
        this.props.onClose();
    };

    render() {
        return (
            <div>

                <ListGroup>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend">
                                <InputGroupText>Topic Name:</InputGroupText>
                            </InputGroupAddon>
                            <Input type="text" name="topicSearch" id="topicAddName"
                                   defaultValue=""
                                   onChange={event => this.setAddTopicName(event.target.value)} />
                        </InputGroup>
                    </ListGroupItem>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend">
                                <InputGroupText>Partitions</InputGroupText>
                            </InputGroupAddon>
                            <Input type="number" name="topicSearch" id="topicAddPartitions"
                                   defaultValue="10"
                                   onChange={event => this.setAddTopicPartition(event.target.value)} />
                        </InputGroup>
                    </ListGroupItem>

                    <ListGroupItem>
                        <InputGroup>
                            <InputGroupAddon addonType="prepend" >
                                <InputGroupText>Replication Factor</InputGroupText>
                            </InputGroupAddon>
                            <Input type="number" name="topicSearch" id="topicAddReplication"
                                   defaultValue="3"
                                   onChange={event => this.setAddTopicReplication(event.target.value)} />
                        </InputGroup>
                    </ListGroupItem>

                    <ListGroupItem>
                        <ButtonGroup>
                            <Button>Create</Button>
                            <Button onClick={this.onClose}>Cancel</Button>
                        </ButtonGroup>
                    </ListGroupItem>

                </ListGroup>

            </div>
        )
    }

}

CreateTopic.propTypes = {
    onClose: PropTypes.func.isRequired,
};


export default CreateTopic;