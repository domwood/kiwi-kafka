import React, { Component } from "react";
import PropTypes from "prop-types";
import ColumnFilterButtons from "./ColumnFilterButtons";
import {Button, ButtonGroup, Input, Label, ListGroup} from "reactstrap";
import ListGroupItem from "reactstrap/es/ListGroupItem";

class FileDownloader extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            format: 'CSV',
            showValue: true,
            showKey: true,
            separator: '\t',
            buttons: [
                {key: 'showKey', displayName: 'Key', example: '234232'},
                {key: 'showValue', displayName: 'Value', example: '{"example","data"}'},
                {key: 'showHeaders', displayName: 'Headers', example: '{"header","one"}'},
                {key: 'showTimestamp', displayName: 'Timestamp', example: '1563951395000'},
                {key: 'showPartition', displayName: 'Partition', example: '3'},
                {key: 'showOffset', displayName: 'Offset', example: '232332'}
            ]
        }
    }

    toggleField = (field) => {
        this.setState({
            [field]: !this.state[field]
        })
    };

    setFormat = (type) => {
        this.setState({
            format: type
        })
    };

    setSeparator = (separator) => {
        console.log(separator);
        this.setState({
            separator: separator
        })
    };

    exampleJsonFile = () => {
        return (
            <ListGroup>
                <ListGroupItem>
                    {JSON.stringify(this.state.buttons.reduce((base, element) =>{
                            if(this.state[element.key]){
                            base[element.displayName] = element.example;
                        }
                        return base;
                    }, {}))}
                </ListGroupItem>
            </ListGroup>

        )
    };

    exampleCsvFile = () => {
        return (
            <ListGroup>
                <ListGroupItem>
                    {this.state.buttons.reduce((base, element) =>{
                        if(this.state[element.key]){
                            base = base + element.example + this.state.separator;
                        }
                        return base;
                    }, '')}
                </ListGroupItem>

                <ListGroupItem>
                    {this.state.showKey ? 'DifferentKey' + this.state.separator : null}
                    {this.state.showValue ? '{\'different\',\'data\'}' + this.state.separator : null}
                    {this.state.showHeaders ? '-' + this.state.separator : null}
                    {this.state.showTimestamp ? '1563951125000' + this.state.separator : null}
                    {this.state.showPartition ? '0' + this.state.separator : null}
                    {this.state.showOffset ? '11' + this.state.separator : null}
                </ListGroupItem>
            </ListGroup>
        )
    };

    render() {
        return (
            <div>
                <div className={"Gap"} />

                <Label>File Format:</Label>

                <div className={"Gap"} />

                <ButtonGroup>
                    <Button onClick={() => this.setFormat('CSV')} outline={this.state.format !== 'CSV'}>
                        CSV
                    </Button>
                    <Button onClick={() => this.setFormat('JSON')} outline={this.state.format !== 'JSON'}>
                        JSON (list)
                    </Button>

                </ButtonGroup>

                <div className={"Gap"} />

                <Label>
                    Include the following data in the file Download:
                </Label>

                <ColumnFilterButtons name={'MessageTableFilter'} id={'MessageTableFilter'} buttons={[
                    {key: 'showKey', displayName: 'Key'},
                    {key: 'showValue', displayName: 'Value'},
                    {key: 'showHeaders', displayName: 'Headers'},
                    {key: 'showTimestamp', displayName: 'Timestamp'},
                    {key: 'showPartition', displayName: 'Partition'},
                    {key: 'showOffset', displayName: 'Offset'}
                ]} viewState={this.state} updater={this.toggleField} />

                <div className={"Gap"} />

                {this.state.format === 'CSV' ?
                    <div>
                        <Label>CSV Separator</Label>
                        <Input value={this.state.separator} onChange={event =>this.setSeparator(event.target.value || '') }/>
                    </div>

                    : null
                }

                <div className={"Gap"} />

                <Label>
                    Example File Format:
                </Label>


                {this.state.format === 'CSV' ? this.exampleCsvFile() : this.exampleJsonFile()}

                <div className={"Gap"} />

                <Button onClick={() => {}} color={"success"} id="consumeToFile" block>Download</Button>
            </div>
        )
    }
}

FileDownloader.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired
};

export default FileDownloader;