import React, {Component} from "react";
import PropTypes from "prop-types";
import {Table} from "reactstrap";
import * as GeneralUtilities from "../../../services/GeneralUtilities";

class PartitionView extends Component {
    render() {
        return (
            <div>
                <Table size="sm" style={{textAlign: "center"}}>
                    <thead>
                    <tr>
                        <th>Number</th>
                        <th>Replication Count</th>
                        <th>Replica Nodes</th>
                        <th>ISRs</th>
                        <th>Leader</th>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        this.props.partitions.map(p => {
                            return (
                                <tr key={this.props.topic + "_" + p.partition}>
                                    <td>{p.partition}</td>
                                    <td>{p.replicationFactor}</td>
                                    <td>{GeneralUtilities.prettyArray(p.replicas)}</td>
                                    <td>{GeneralUtilities.prettyArray(p.isrs)}</td>
                                    <td>{p.leader}</td>
                                </tr>
                            )
                    })}
                    </tbody>
                </Table>
            </div>
        )
    }
}

PartitionView.propTypes = {
    topic: PropTypes.string.isRequired,
    partitions: PropTypes.array.isRequired
};


export default PartitionView ;