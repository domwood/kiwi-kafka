import React, { Component } from "react";
import {Table} from "reactstrap";
import PropTypes from "prop-types";
import * as GeneralUtilities from "../../../services/GeneralUtilities";


class MessageTable extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
        }
    }

    render() {
        return (
            <div>
                {
                    this.props.messages.length > 0 ?
                        <Table size="sm" bordered >
                            <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Partition</th>
                                <th>Offset</th>
                                <th>Key</th>
                                <th>Headers</th>
                                <th>Message</th>
                            </tr>
                            </thead>
                            <tbody className="WrappedTable">
                            {
                                this.props.messages.map(m => {
                                    return (
                                        <tr key={`${m.partition}_${m.offset}`} id={`record_row_${m.partition}_${m.offset}`}>
                                            <td width="10%">{m.timestamp}</td>
                                            <td width="8%">{m.partition}</td>
                                            <td width="6%">{m.offset}</td>
                                            <td width="10%">{m.key}</td>
                                            <td width="20%">{GeneralUtilities.isEmpty(m.headers) ? "" : JSON.stringify(m.headers)}</td>
                                            <td width="46%">{m.message}</td>
                                        </tr>
                                    )
                                })
                            }
                            </tbody>
                        </Table> : ''
                }
            </div>
        )
    }
}

MessageTable.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    messages: PropTypes.array.isRequired
};

export default MessageTable;