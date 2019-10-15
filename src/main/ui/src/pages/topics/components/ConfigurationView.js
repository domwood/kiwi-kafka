import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, Input, InputGroup, InputGroupAddon, Table, Tooltip} from "reactstrap";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";

class ConfigurationView extends Component {
    constructor(props) {
        super(props);

        this.state = {
            editKey: "",
            editValue: "",
            configuration: props.configuration,
            disabledToolTip: false
        }
    }

    isEditDisabled = () => {
        let profiles = this.props.profiles||[];
        return profiles.length !== 0 && profiles.indexOf("write-admin") === -1;
    };

    closeToolTip = () => {
        this.setState({
            disabledToolTip: !this.state.disabledToolTip
        })
    };

    editConfig = (key, value) => {
        this.setState({
            editKey:key,
            editValue: value
        })
    };

    onEditUpdate = (value) => {
        this.setState({
            editValue: value
        })
    };

    save = () => {
        if(this.state.editValue){
            let configUpdate = {[this.state.editKey]: this.state.editValue};
            ApiService.updateTopicConfig(this.props.topic, configUpdate,
                () => {
                    this.setState({
                        configuration:Object.assign(this.state.configuration, configUpdate),
                        editKey: '',
                        editValue: '',

                    }, () => {
                        toast.info("Updated Topic configuration");
                    })
                },
                (err => toast.error(`Failed to update configuration ${err.message}`))
            )
        }
    };

    render() {
        return (
            <div>
                <Table size="sm">
                    <thead>
                    <tr>
                        <th width="40%">Config Key</th>
                        <th width="40%">Config Value</th>
                        <th width="20%"></th>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        Object.keys(this.state.configuration).map(key => {
                            return (
                                <tr key={`${this.props.topic}_${key}`}>
                                    <td>{key}</td>
                                    <td>{this.state.configuration[key]}</td>
                                    <td>
                                        {
                                            this.state.editKey !== key ?
                                                <Button id={"Edit"+this.props.topic} color="secondary" onClick={() => this.editConfig(key)} disabled={!!this.state.editKey || this.isEditDisabled()}>Edit</Button> :
                                                <div>
                                                    <InputGroup>
                                                        <Input type="text" defaultValue={this.state.configuration[key]} onChange={event => this.onEditUpdate(event.target.value)}/>
                                                        <InputGroupAddon addonType="append">
                                                            <Button color="success" onClick={() => this.save()}>Save</Button>
                                                        </InputGroupAddon>
                                                        <InputGroupAddon addonType="append">
                                                            <Button color="secondary" onClick={() => this.editConfig('')}>Cancel</Button>
                                                        </InputGroupAddon>

                                                    </InputGroup>
                                                </div>
                                        }

                                        <Tooltip placement="right" isOpen={this.state.disabledToolTip} target={"Edit"+this.props.topic} toggle={this.closeToolTip}>
                                            {this.isEditDisabled() ? '[Disabled] To enable restart kiwi with admin-write profile' : 'Edit this configuration value'}
                                        </Tooltip>

                                    </td>
                                </tr>
                            )
                        })
                    }
                    </tbody>
                </Table>
            </div>
        )
    }
}

ConfigurationView.propTypes = {
    topic: PropTypes.string.isRequired,
    configuration: PropTypes.object.isRequired,
    profiles: PropTypes.array
};


export default ConfigurationView ;