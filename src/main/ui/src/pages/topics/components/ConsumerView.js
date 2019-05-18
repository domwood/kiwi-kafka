import React, {Component} from "react";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import PropTypes from "prop-types";
import ConsumerGroupView from "../../groups/components/ConsumerGroupView";
import {MdRefresh} from "react-icons/md";
import {Button} from "reactstrap";

class ConsumerView extends Component {
    constructor(props) {
        super(props);

        this.state = {
            groups: []
        }
    }

    componentDidMount() {
        this.getConsumerGroups();
    }

    getConsumerGroups = () => {
        ApiService.consumerGroupsForTopic(this.props.topic, (data) => {
            this.setState({
                groups: data || []
            });
        }, (err) => toast.error(`Error retrieving list of groups for ${this.props.topic}: ${err.message}`))
    };

    render() {
        return (
            <div>
                {this.state.groups.length === 0 ? <Button color="primary" onClick={this.getConsumerGroups}>Refresh <MdRefresh/></Button> : ''}
                {this.state.groups.length === 0 ? 'No active consumers found.' : ''}
                {this.state.groups.map(group => <ConsumerGroupView key={`${group}_${this.props.topic}_view`} groupId={group} topics={[this.props.topic]} />)}
            </div>
        )
    }
}

ConsumerView.propTypes = {
    topic: PropTypes.string.isRequired
};


export default ConsumerView ;