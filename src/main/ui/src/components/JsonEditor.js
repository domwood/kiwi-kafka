import React, { Component } from "react";
import {Input, Label} from "reactstrap";
import FormGroup from "reactstrap/es/FormGroup";


class JsonEditor extends Component {
    constructor(props) {
        super(props);
        this.state = {
            id : props.id,
            name : props.name,
            local: ""
        };
        this.addAlert = this.addAlert.bind(this);
        this.format = this.format.bind(this);
        this.updateState = this.updateState.bind(this);
    }

    addAlert(alert){
        this.props.addAlert(alert);
    }

    updateState(event){
        this.props.updateMessage(event.target.value);
    }

    format(messagedata, pretty) {
        try {
            if (/^{/.test(messagedata)) {
                let obj = JSON.parse(messagedata);
                if (pretty) {
                    return JSON.stringify(obj, undefined, 4);
                }

                return JSON.stringify(obj);
            }
        } catch (err) {
            this.addAlert({
                text: 'Cannot format message which is not valid json',
                class: 'primary'
            });
        }

        return messagedata;
    }

    render() {
        return (
            <FormGroup>
                <Label for="exampleText">Input:</Label>
                <Input type="textarea"
                       name={this.state.name}
                       id={this.state.id}
                       bsSize="large"
                       rows={30}
                       height="400px"
                       onChange={this.updateState}
                       value={this.state.local}
                />
                <Label for="exampleFile">Send Message From File:</Label>
                <Input type="file" name="file" id="exampleFile" />
            </FormGroup>
        )
    };
}

export default JsonEditor;