import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, ButtonToolbar} from "reactstrap";
import {AppDataContext, CLOSED_STATE} from "../../../contexts/AppDataContext";

class PartitionConfigurer extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            isDisabled: false,
            toggleFilter: false,
            activePartitions: {}
        };
    }

    togglePartitionView = () => {
        this.setState({
            toggleFilter: !this.state.toggleFilter,
            activePartitions: []
        }, this.props.onUpdate([]));
    }

    setPartition = (partition) => {
        let activePartitionUpdated = this.state.activePartitions;
        activePartitionUpdated[partition] = !activePartitionUpdated[partition];
        this.setState({
            activePartitions: activePartitionUpdated,
        }, this.props.onUpdate(Object.keys(this.state.activePartitions).filter(partition => this.state.activePartitions[partition])));
    }

    render() {
        let partitionCount = (this.context.topicData[this.context.targetTopic] || {}).partitionCount;
        let disabled = this.context.consumingState !== CLOSED_STATE;

        if (partitionCount > -1 && this.state.toggleFilter) {
            return (
                <ButtonToolbar style={{width: "100%", justifyContent: "center"}}>
                    <div style={{marginTop: "5px"}}/>
                    {Array.from(Array(partitionCount).keys()).map(partition => (
                        <ButtonGroup key={"group_" + partition}>
                            <Button key={"button_" + partition}
                                    size="sm"
                                    style={{width: "40px"}}
                                    outline={!this.state.activePartitions[partition]}
                                    onClick={() => this.setPartition(partition)}
                                    disabled={disabled}>
                                {partition}
                            </Button>
                        </ButtonGroup>
                    ))}
                    <ButtonGroup key={"group_clear"}>
                        <Button key={"button_clear"}
                                size="sm"
                                style={{width: "40px"}}
                                outline={Object.keys(this.state.activePartitions).length > 0}
                                onClick={this.togglePartitionView}
                                disabled={disabled}>
                            <b>All</b>
                        </Button>
                    </ButtonGroup>
                </ButtonToolbar>
            )
        } else if (partitionCount > -1 && !this.state.toggleFilter) {
            return (
                <Button color="secondary" size="sm" block
                        onClick={this.togglePartitionView}
                        disabled={disabled}>
                    Include Partition Filter
                </Button>
            )
        } else {
            return (<React.Fragment/>)
        }
    }
}

PartitionConfigurer.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    onUpdate: PropTypes.func.isRequired
};

export default PartitionConfigurer;