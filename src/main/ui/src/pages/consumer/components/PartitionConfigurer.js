import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, ButtonToolbar} from "reactstrap";
import {AppDataContext} from "../../../contexts/AppDataContext";

class PartitionConfigurer extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            toggleFilter: false,
            activePartitions: {}
        };
    }

    togglePartitionView = () => {
        this.setState({
            toggleFilter: !this.state.toggleFilter,
            activePartitions: []
        })
    }

    setPartition = (partition) => {
        let activePartitionUpdated = this.state.activePartitions;
        activePartitionUpdated[partition] = !activePartitionUpdated[partition];
        this.setState({
            activePartitions: activePartitionUpdated,
        });
        this.props.onUpdate(Object.keys(this.state.activePartitions).filter(partition => this.state.activePartitions[partition]))
    }

    render() {
        let partitionCount = (this.context.topicData[this.context.targetTopic] || {}).partitionCount;
        let buttons = Array.from(Array(partitionCount).keys()).map(partition => (
            <ButtonGroup key={"group_" + partition}>
                <Button key={"button_" + partition}
                        size="sm"
                        outline={!this.state.activePartitions[partition]}
                        onClick={() => this.setPartition(partition)}>
                    {partition}
                </Button>
            </ButtonGroup>
        ));

        return (
            partitionCount > -1 ?
                <React.Fragment>
                    {this.state.toggleFilter ?
                        <ButtonToolbar style={{width: "100%", justifyContent: "center"}}>
                            <div style={{marginTop: "5px"}}/>
                            {buttons}
                            <ButtonGroup key={"group_clear"}>
                                <Button key={"button_clear"}
                                        size="sm"
                                        outline={Object.keys(this.state.activePartitions).length > 0}
                                        onClick={this.togglePartitionView}>
                                    <b>All</b>
                                </Button>
                            </ButtonGroup>
                        </ButtonToolbar> :
                        <Button color="secondary" size="sm" block
                                onClick={this.togglePartitionView}>
                            Include Partition Filter
                        </Button>
                    }
                </React.Fragment>
                : <React.Fragment/>
        )
    }
}

PartitionConfigurer.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    onUpdate: PropTypes.func.isRequired
};

export default PartitionConfigurer;