import React, {Component} from "react";
import PropTypes from "prop-types";

class ConsumerGroupTopicView extends Component {
    render() {
        return (
            <tr key={`${this.props.topic}_${this.props.groupId}_${this.props.assignment.partition}`}>
                <td>{this.props.assignment.groupId}</td>
                <td>{this.props.assignment.partition}</td>
                <td>{this.props.assignment.consumerId}</td>
                <td>{this.props.assignment.groupState || 'INACTIVE'}</td>
                <td>{this.props.assignment.offset.partitionOffset}</td>
                <td>{this.props.assignment.offset.groupOffset}</td>
                <td>{this.props.assignment.offset.lag}</td>
                <td>{this.props.assignment.coordinator}</td>
            </tr>
        )
    }
}

ConsumerGroupTopicView.propTypes = {
    groupId: PropTypes.string.isRequired,
    topic: PropTypes.string.isRequired,
    assignment: PropTypes.object.isRequired
};


export default ConsumerGroupTopicView ;