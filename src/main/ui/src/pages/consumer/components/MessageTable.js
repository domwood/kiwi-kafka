import React, { Component } from "react";
import {Button, ButtonGroup, Table} from "reactstrap";
import PropTypes from "prop-types";
import * as GeneralUtilities from "../../../services/GeneralUtilities";
import ColumnFilterButtons from "./ColumnFilterButtons";


class MessageTable extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,

            showTimestamp: true,
            showDateTime: false,
            showPartition: true,
            showOffset: true,
            showKey: true,
            showHeaders: true,
            showValue: true
        }
    }

    toggleField = (field) => {
        this.setState({
            [field]: !this.state[field]
        })
    };

    render() {
        return (
                this.props.messages.length > 0 ?
                    <div>
                        <div className={"TwoGap"} />

                        <ColumnFilterButtons name={'MessageTableFilter'} id={'MessageTableFilter'} buttons={[
                            {key: 'showTimestamp', displayName: 'Timestamp'},
                            {key: 'showDateTime', displayName: 'Date&Time'},
                            {key: 'showPartition', displayName: 'Partition'},
                            {key: 'showOffset', displayName: 'Offset'},
                            {key: 'showKey', displayName: 'Key'},
                            {key: 'showHeaders', displayName: 'Headers'},
                            {key: 'showValue', displayName: 'Value'}
                        ]} viewState={this.state} updater={this.toggleField} />

                        <div className={"TwoGap"} />

                        <Table size="sm" bordered >
                            <thead>
                            <tr>
                                {this.state.showTimestamp ? <th>Timestamp</th> : null}
                                {this.state.showDateTime ?  <th>Date&Time</th> : null}
                                {this.state.showPartition ? <th>Partition</th> : null}
                                {this.state.showOffset ?    <th>Offset</th> : null}
                                {this.state.showKey ?       <th>Key</th> : null}
                                {this.state.showHeaders ?   <th>Headers</th> : null}
                                {this.state.showValue ?     <th>Value</th> : null}
                            </tr>
                            </thead>
                            <tbody className="WrappedTable">
                            {
                                this.props.messages.map(m => {
                                    return (
                                        <tr key={`${m.partition}_${m.offset}`} id={`record_row_${m.partition}_${m.offset}`}>
                                            {this.state.showTimestamp ? <td width="10%">{m.timestamp}</td> : null}
                                            {this.state.showDateTime ?  <td width="10%">{GeneralUtilities.prettyTimestamp(m.timestamp)}</td> : null}
                                            {this.state.showPartition ? <td width="3%">{m.partition}</td>  : null}
                                            {this.state.showOffset ?    <td width="3%">{m.offset}</td>     : null}
                                            {this.state.showKey ?       <td width="10%">{m.key}</td> : null}
                                            {this.state.showHeaders ?   <td width="18%">{GeneralUtilities.isEmpty(m.headers) ? "" : JSON.stringify(m.headers)}</td> : null}
                                            {this.state.showValue ?     <td width="46%">{m.message}</td>: null}
                                        </tr>
                                    )
                                })
                            }
                            </tbody>
                        </Table>
                    </div> :
                    <div></div>
        )
    }
}

MessageTable.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    messages: PropTypes.array.isRequired
};

export default MessageTable;