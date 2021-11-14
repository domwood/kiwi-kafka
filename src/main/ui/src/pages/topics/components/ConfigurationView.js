import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, Input, InputGroup, InputGroupText, Table} from "reactstrap";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import ProfileToggleToolTip from "../../common/ProfileToggleToolTip";

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
        let profiles = this.props.profiles || [];
        return profiles.length !== 0 && profiles.indexOf("write-admin") === -1;
    };

    editConfig = (key, value) => {
        this.setState({
            editKey: key,
            editValue: value
        })
    };

    onEditUpdate = (value) => {
        this.setState({
            editValue: value
        })
    };

    save = () => {
        if (this.state.editValue) {
            let currentNonDefaults = Object.keys(this.state.configuration)
                .filter(key => !this.state.configuration[key].isDefault)
                .reduce((base, key) => {
                    base[key] = this.state.configuration[key].configValue;
                    return base
                }, {})
            let update = Object.assign(currentNonDefaults, {[this.state.editKey]: this.state.editValue})
            ApiService.updateTopicConfig(this.props.topic, update,
                () => {
                    let configUpdate = {[this.state.editKey]: {configValue: this.state.editValue, isDefault: false}}
                    this.setState({
                        configuration: Object.assign(this.state.configuration, configUpdate),
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
                        <th width="35%">Config Key</th>
                        <th width="35%">Config Value</th>
                        <th width="10%">Default Value</th>
                        <th width="20%" />
                    </tr>
                    </thead>
                    <tbody>
                    {
                        Object.keys(this.state.configuration).map((key, index) => {

                            return (
                                <tr key={`${this.props.topic}_${key}`}>
                                    <td>{key}</td>
                                    <td>{this.state.configuration[key].configValue}</td>
                                    <td>{this.state.configuration[key].isDefault ? 'Yes' : <b>No</b>}</td>
                                    <td>
                                        {
                                            this.state.editKey !== key ?
                                                <div>
                                                    <Button
                                                        id={"Edit" + this.props.topic}
                                                        color="success"
                                                        onClick={() => this.editConfig(key)}
                                                        disabled={!!this.state.editKey || this.isEditDisabled()}>Edit
                                                    </Button>
                                                    <ProfileToggleToolTip id={`${this.props.topic}_${index}_pop`}
                                                                          profiles={this.props.profiles}
                                                                          targetProfile={"write-admin"}
                                                    />
                                                </div>
                                                :
                                                <div>
                                                    <InputGroup>
                                                        <Input type="text"
                                                               defaultValue={this.state.configuration[key].configValue}
                                                               onChange={event => this.onEditUpdate(event.target.value)}/>
                                                        <InputGroupText addonType="append">
                                                            <Button color="success"
                                                                    onClick={() => this.save()}>Save</Button>
                                                        </InputGroupText>
                                                        <InputGroupText addonType="append">
                                                            <Button color="secondary"
                                                                    onClick={() => this.editConfig('')}>Cancel</Button>
                                                        </InputGroupText>

                                                    </InputGroup>
                                                </div>
                                        }
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


export default ConfigurationView;