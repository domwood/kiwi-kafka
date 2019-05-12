import React, { Component } from "react";
import {
    ButtonDropdown,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Input,
    InputGroup,
    InputGroupAddon,
    Label
} from "reactstrap";
import PropTypes from "prop-types";

class MessageLimit extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            messageStartToggle: false
        }
    }

    toggleMessageStartDropdown = () => {
        this.setState({messageStartToggle:!this.state.messageStartToggle})
    };

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
                    <InputGroupAddon addonType="append">
                        <ButtonDropdown direction="down"
                                        isOpen={this.state.messageStartToggle}
                                        toggle={this.toggleMessageStartDropdown}>
                            <DropdownToggle caret>
                                {this.props.messageFromEnd ? "From End" : "From Start"}
                            </DropdownToggle>
                            <DropdownMenu>
                                <DropdownItem onClick={() => this.props.onMessageEndUpdate(true)}>Limit From End</DropdownItem>
                                <DropdownItem onClick={() => this.props.onMessageEndUpdate(false)}>Limit From Start</DropdownItem>
                            </DropdownMenu>
                        </ButtonDropdown>
                    </InputGroupAddon>
                </InputGroup>
            </div>
        )
    }
}

MessageLimit.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    onMessageEndUpdate: PropTypes.func.isRequired,
    onMessageLimitUpdate: PropTypes.func.isRequired,
    messageLimit: PropTypes.number.isRequired,
    messageFromEnd: PropTypes.bool.isRequired
};

export default MessageLimit;