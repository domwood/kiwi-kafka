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
        };
    }

    render() {
        let partitionCount = (this.context.topicData[this.state.targetTopic] || {}).partitionCount;
        return (
            partitionCount > -1 ?
                <ButtonToolbar>
                    <ButtonGroup>
                        {Array.from(Array(partitionCount).keys())
                            .map(partition => (<Button key={partition}>{partition}</Button>))}
                    </ButtonGroup>
                </ButtonToolbar>
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