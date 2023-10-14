import React, {Component} from "react";
import {Input, InputGroup, InputGroupText} from "reactstrap";
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
                <InputGroup>
                    <InputGroupText className="input-group-text-padded">
                        Message Limit:
                    </InputGroupText>
                    <Input type="number"
                           name="messageLimit"
                           id="messageLimitInput"
                           defaultValue={this.props.messageLimit}
                           onChange={event => this.props.onMessageLimitUpdate(parseInt(event.target.value || 0))}
                           min="1"
                           required
                    />
                    <InputGroupText className="input-group-text-padded">
                        Pause After Match Count
                    </InputGroupText>
                    <Input type="number"
                           name="messageLimit"
                           id="pauseAfterCount"
                           defaultValue={this.props.pauseAfterMatchCount}
                           onChange={event => this.props.onPauseAfterMatchCountUpdate(parseInt(event.target.value || 0))}
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
    messageLimit: PropTypes.number.isRequired,
    onPauseAfterMatchCountUpdate: PropTypes.func.isRequired,
    pauseAfterMatchCount: PropTypes.number
};

export default MessageLimit;