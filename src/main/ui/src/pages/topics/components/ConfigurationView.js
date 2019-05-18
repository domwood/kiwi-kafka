import React, {Component} from "react";
import PropTypes from "prop-types";
import {Table} from "reactstrap";

class ConfigurationView extends Component {
    render() {
        return (
            <div>
                <Table size="sm">
                    <thead>
                    <tr>
                        <th>Config Key</th>
                        <th>Config Value</th>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        Object.keys(this.props.configuration).map(key => {
                            return (
                                <tr key={`${this.props.topic}_${key}`}>
                                    <td>{key}</td>
                                    <td>{this.props.configuration[key]}</td>
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
    configuration: PropTypes.object.isRequired
};


export default ConfigurationView ;