import React, { Component } from "react";
import {Button, Table} from "reactstrap";
import PropTypes from "prop-types";
import * as GeneralUtilities from "../../../services/GeneralUtilities";
import ColumnFilterButtons from "./ColumnFilterButtons";
import {GoClippy} from "react-icons/go";
import {toast} from "react-toastify";

class MessageTable extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,

            showTimestamp: true,
            showDateTime: true,
            showPartition: true,
            showOffset: true,
            showKey: true,
            showHeaders: true,
            showValue: true,
            buttons: [
                {key: 'showTimestamp', displayName: 'Timestamp'},
                {key: 'showDateTime', displayName: 'Date&Time'},
                {key: 'showPartition', displayName: 'Partition'},
                {key: 'showOffset', displayName: 'Offset'},
                {key: 'showKey', displayName: 'Key'},
                {key: 'showHeaders', displayName: 'Headers'},
                {key: 'showValue', displayName: 'Value'}
            ]
        }
    }

    toggleField = (field) => {
        this.setState({
            [field]: !this.state[field]
        });
    };

    isLastHeader= (button) => {
        return this.state.buttons
            .filter(b => this.state[b.key])
            .reduce((a,b) => b).key === button.key;
    };

    copyViewToClipboard = () => {
        let el = document.getElementById('copiableTableBody')
        let body = document.body, range, sel;
        if (document.createRange && window.getSelection) {
            range = document.createRange();
            sel = window.getSelection();
            sel.removeAllRanges();
            try {
                range.selectNodeContents(el);
                sel.addRange(range);
            } catch (e) {
                range.selectNode(el);
                sel.addRange(range);
            }
        } else if (body.createTextRange) {
            range = body.createTextRange();
            range.moveToElementText(el);
            range.select();
        }

        let ok = document.execCommand('copy')
        if (ok) {
            toast.success("Copied current table view to clipboard");
        }
        else{
            toast.error("Failed to table to clipboard");
        }
    };

    render() {
        return (
                this.props.messages.length > 0 ?
                    <div>
                        <div className={"TwoGap"} />

                        <ColumnFilterButtons name={'MessageTableFilter'}
                                             id={'MessageTableFilter'}
                                             buttons={this.state.buttons}
                                             viewState={this.state}
                                             updater={this.toggleField} />

                        <div className={"Gap"} />

                        <Table size="sm" id={'mainTable'} bordered >
                            <thead>
                            <tr>
                                {this.state.buttons.map((button) => {
                                    return this.state[button.key] ?
                                        <th key={'table_th_'+button.displayName}>
                                            <span style={{"float":"left"}}>{button.displayName}</span>
                                            {this.isLastHeader(button) ?
                                                <span style={{"float":"right", "margin" : "-4px"}}>
                                                    <Button onClick={this.copyViewToClipboard} size={"sm"}><GoClippy /></Button>
                                                </span>: null}
                                        </th> : null
                                })}
                            </tr>
                            </thead>
                            <tbody className="WrappedTable" id={'copiableTableBody'}>
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