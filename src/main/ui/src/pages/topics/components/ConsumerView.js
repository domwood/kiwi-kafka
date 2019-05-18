import React, {Component} from "react";
import PropTypes from "prop-types";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import ConsumerGroupDetailsView from "../../groups/components/ConsumerGroupDetailsView";
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
        ApiService.getConsumerGroupsForTopic(this.props.topic, (data) => {
            this.setState({
                groups: data || []
            });
        }, (err) => toast.error(`Error retrieving list of groups for ${this.props.topic}: ${err.message}`))
    };

    render() {
        return (
            <div>

                {this.state.groups.length === 0 ? <Button color="primary" onClick={this.getConsumerGroups}>Refresh <MdRefresh/></Button> : ''}
                {this.state.groups.length === 0 ?
                    <div>
                        <div className={"Gap"}/>
                        <div>No active consumers found.</div>
                    </div> : ''}
                {this.state.groups.map(group => <ConsumerGroupDetailsView key={`${group}_${this.props.topic}_view`} groupId={group} topics={[this.props.topic]} />)}
            </div>
        )
    }
}

ConsumerView.propTypes = {
    topic: PropTypes.string.isRequired
};


export default ConsumerView ;