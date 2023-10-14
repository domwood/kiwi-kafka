import React, { Component } from "react";
import PropTypes from "prop-types";
import ColumnFilterButtons from "./ColumnFilterButtons";
import {Button, ButtonGroup, Input, Label, ListGroup, ListGroupItem} from "reactstrap";
import {toast} from "react-toastify";
import * as ApiService from "../../../services/ApiService";
import ProfileToggleToolTip from "../../common/ProfileToggleToolTip";
import {AppDataContext} from "../../../contexts/AppDataContext";

class FileDownloader extends Component {

    static contextType = AppDataContext

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
                {key: 'showKey', displayName: 'Key', example: 'a78287b2-b118-11e9-ad57-2bd2664ff578'},
                {key: 'showTimestamp', displayName: 'Timestamp', example: '1563951395000'},
                {key: 'showPartition', displayName: 'Partition', example: '3'},
                {key: 'showOffset', displayName: 'Offset', example: '232332'},
                {key: 'showHeaders', displayName: 'Headers', example: {example: "header"}},
                {key: 'showValue', displayName: 'Value', example: '{"example","data"}'}
            ]
        }
    }

    isConsumerDisabled = () => {
        let profiles = this.props.profiles||[];
        return profiles.length !== 0 && profiles.indexOf("read-consumer") === -1;
    };

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
                            let exampleColumn = element.example instanceof Object ? JSON.stringify(element.example) : element.example;
                            base = base + exampleColumn + this.state.separator;
                        }
                        return base;
                    }, '')}
                </ListGroupItem>
            </ListGroup>
        )
    };

    postForDownload = () => {
        ApiService.consumeToFile(
            [this.context.targetTopic],
            this.props.filters,
            this.state.format,
            this.state.buttons.filter(b => this.state[b.key]).map(b => b.displayName.toUpperCase()),
            this.state.separator,
            () => toast.error("Failed to download data to file"));
    };

    render() {
        return (
            <div>
                <div className={"Gap"} />

                <Label>File Format: &nbsp;</Label>

                <ButtonGroup>
                    <Button onClick={() => this.setFormat('CSV')} outline={this.state.format !== 'CSV'}>
                        CSV
                    </Button>
                    <Button onClick={() => this.setFormat('JSON')} outline={this.state.format !== 'JSON'}>
                        JSON (list)
                    </Button>

                </ButtonGroup>

                {this.props.filters.length > 0 ?
                    <div>
                        <div className={"Gap"} />

                        ( {this.props.filters.length} Filters will be applied to the downloading data )
                    </div> :null}

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
                    Example format for each line:
                </Label>


                {this.state.format === 'CSV' ? this.exampleCsvFile() : this.exampleJsonFile()}

                <div className={"Gap"} />

                <Label>
                    The following columns will be included in the file download:
                </Label>

                <ColumnFilterButtons name={'MessageTableFilter'} id={'MessageTableFilter'} buttons={this.state.buttons} viewState={this.state} updater={this.toggleField} />

                <div className={"Gap"} />


                <Button onClick={this.postForDownload}
                        color={"success"}
                        id="consumeToFile"
                        disabled={(!this.context.targetTopic || this.context.targetTopic.length === 0) || this.isConsumerDisabled()}
                        block>Download</Button>
                <ProfileToggleToolTip profiles={this.props.profiles}
                                      targetProfile={"read-consumer"}
                                      id={`${this.context.targetTopic}_fd`}
                                      style={{"float":"right", "marginRight":"-20px", "marginTop":"-31px"}}
                />

            </div>
        )
    }
}

FileDownloader.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    filters: PropTypes.array.isRequired,
    profiles: PropTypes.array.isRequired
};

export default FileDownloader;