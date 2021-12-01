import React, {Component} from "react";
import {FormGroup, Input, InputGroup, InputGroupText, Label} from "reactstrap";
import PropTypes from 'prop-types';
import {toast} from "react-toastify";

class JsonEditor extends Component {
    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            local: "",
            nullMessage: false
        };
    }

    error = (msg) => toast.error(msg);

    updateState = (event) => {
        let local = event.target.value;
        let toSend = this.state.nullMessage ? null : local
        this.setState({
            local: event.target.value
        }, () => this.props.updateMessage(toSend));
    };

    toggleNullMessage = () => {
        this.setState({
            nullMessage: !this.state.nullMessage
        }, () => this.props.updateMessage(this.state.nullMessage ? null : this.state.local));


    };

    format = (messagedata, pretty) => {
        try {
            if (/^{/.test(messagedata)) {
                let obj = JSON.parse(messagedata);
                if (pretty) {
                    return JSON.stringify(obj, undefined, 4);
                }

                return JSON.stringify(obj);
            }
        } catch (err) {
            this.error('Cannot format message which is not valid json');
        }

        return messagedata;
    };

    render() {
        return (
            <FormGroup>
                <Label>Input:</Label>
                <InputGroup>
                    <InputGroupText className={"input-group-text-padded"}>
                        <Input addon type="checkbox"
                               aria-label="Send value of null"
                               name="nullRecordCheckbox"
                               id="nullRecordCheckbox"
                               value={this.state.nullMessage}
                               onChange={this.toggleNullMessage}
                        />
                    </InputGroupText>
                    <Input placeholder={"Send Null Message Payload"} disabled></Input>
                </InputGroup>
                <div className={"Gap"}/>
                {
                    this.state.nullMessage ? null :
                        <Input type="textarea"
                               name={this.state.name}
                               id={this.state.id}
                               bsSize="large"
                               rows={15}
                               height="200px"
                               onChange={this.updateState}
                               value={this.state.local}
                        />
                }

            </FormGroup>
        )
    }
}

JsonEditor.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    updateMessage: PropTypes.func.isRequired
};

export default JsonEditor;