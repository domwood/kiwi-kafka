import React, { Component } from "react";
import PropTypes from "prop-types";
import ColumnFilterButtons from "./ColumnFilterButtons";
import {Button, ButtonGroup, Input, Label} from "reactstrap";

class FileDownloader extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            format: 'CSV',
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
            <div>

                <Label>File Format:</Label>
                <div className={"Gap"} />
                <ButtonGroup>
                    <Button>
                        CSV
                    </Button>
                    <Button outline>
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
                        <Input placeholder="\t" />
                    </div>

                    : null
                }

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