import React, { Component } from "react";
import {Input, Label} from "reactstrap";
import FormGroup from "reactstrap/es/FormGroup";
import PropTypes from 'prop-types';
import {toast} from "react-toastify";


class JsonEditor extends Component {
    constructor(props) {
        super(props);
        this.state = {
            id : props.id,
            name : props.name,
            local: ""
        };
    }

    error = (msg) => toast.error(msg);

    updateState = (event) => {
        let newValue = event.target.value;
        this.setState({
            local: newValue
        }, () => this.props.updateMessage(newValue));
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
                <Label for="exampleText">Input:</Label>
                <Input type="textarea"
                       name={this.state.name}
                       id={this.state.id}
                       bsSize="large"
                       rows={15}
                       height="200px"
                       onChange={this.updateState}
                       value={this.state.local}
                />
                {/*<Label for="exampleFile">Send Message From File:</Label>
                <Input type="file" name="file" id="exampleFile" />*/}
            </FormGroup>
        )
    }
}

JsonEditor.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    addAlert: PropTypes.func.isRequired,
    updateMessage: PropTypes.func.isRequired
};

export default JsonEditor;