import React, {Component} from "react";
import PropTypes from "prop-types";

class ConsumerGroupTopicDetailsView extends Component {
    render() {
        return (
            <tr key={`${this.props.topic}_${this.props.groupId}_${this.props.assignment.partition}`}>
                <td>{this.props.assignment.groupId}</td>
                <td>{this.props.topic}-{this.props.assignment.partition}</td>
                <td>{this.props.assignment.consumerId}</td>
                <td>{this.props.assignment.groupState || 'INACTIVE'}</td>
                <td>{this.props.assignment.offset.partitionOffset}</td>
                <td>{this.props.assignment.offset.groupOffset > 0 ? this.props.assignment.offset.groupOffset : 'No Commit'}</td>
                <td>{this.props.assignment.offset.groupOffset > 0 ? this.props.assignment.offset.lag : 'No Commit'}</td>
                <td>{this.props.assignment.coordinator}</td>
            </tr>
        )
    }
}

ConsumerGroupTopicDetailsView.propTypes = {
    groupId: PropTypes.string.isRequired,
    topic: PropTypes.string.isRequired,
    assignment: PropTypes.object.isRequired
};


export default ConsumerGroupTopicDetailsView ;