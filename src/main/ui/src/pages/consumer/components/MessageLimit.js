import React, { Component } from "react";
import {
    Input,
    InputGroup,
    Label
} from "reactstrap";
import PropTypes from "prop-types";

class MessageLimit extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name
        }
    }

    render() {
        return (
            <div>
                <Label for="messageLimit">Message Limit</Label>
                <InputGroup>
                    <Input type="number"
                           name="messageLimit"
                           id="messageLimitInput"
                           defaultValue={this.props.messageLimit}
                           onChange={event => this.props.onMessageLimitUpdate(parseInt(event.target.value||0))}
                           min="1"
                           required
                    />
                </InputGroup>
            </div>
        )
    }
}

MessageLimit.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    onMessageLimitUpdate: PropTypes.func.isRequired,
};

export default MessageLimit;