import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, Spinner, Table} from "reactstrap";
import {MdRefresh} from "react-icons/md";
import {toast} from "react-toastify";
import * as ApiService from "../../../services/ApiService";
import ConsumerGroupTopicDetailsView from "./ConsumerGroupTopicDetailsView";

class ConsumerGroupDetailsView extends Component {
    constructor(props) {
        super(props);

        this.state = {
            groupData: {},
            loading: true
        }
    }

    componentDidMount() {
        this.loadConsumerDetails();
    }

    loadConsumerDetails = () => {
        this.setState({
            loading:true
        }, this.getConsumerGroupDetails);
    };

    getConsumerGroupDetails = () => {
        ApiService.getConsumerGroupDetailsWithOffsets(this.props.groupId, (data) => {
            this.setState({
                groupData:data,
                loading: false
            });
            toast.info(`Retrieved data for groupId ${this.props.groupId}`)
        }, (err) => {
            this.setState({loading:false});
            toast.error(`Error retreiving ${this.props.groupId} group info: ${err.message}`)
        })
    };

    render() {
        return (
            <div>
                <Button color="primary" onClick={this.getConsumerGroupDetails}>Refresh <MdRefresh/></Button>
                {this.state.loading ? <Spinner color="secondary"/> : ''}
                <div className={"Gap"} />
                <Table key={`${this.props.groupId}_table`} size="sm">
                    <thead>
                        <tr>
                            <th>GroupId</th>
                            <th>Partition</th>
                            <th>ConsumerId</th>
                            <th>Group State</th>
                            <th>Partition Offset</th>
                            <th>Consumer Offset</th>
                            <th>Consumer Lag</th>
                            <th>Coordinator</th>
                        </tr>
                    </thead>
                    {Object.entries(this.state.groupData)
                        .filter(([topic, data]) => !this.props.topics || this.props.topics.includes(topic))
                        .map(([topic, data]) => {
                            return (
                                <tbody key={`${this.props.groupId}_${topic}`}>
                                    {(this.props.topics||[]).length !== 1 ?
                                        <tr className="table-primary" key={`${topic}_header_row`}>
                                            <td colspan="8" style={{"text-align":"center"}}>{topic}</td>
                                        </tr> : <tr className="table-primary" key={`${topic}_header_row`} />
                                    }
                                    {
                                        data.map(assignment => (<ConsumerGroupTopicDetailsView key={`${this.props.groupId}_${assignment.partition}_topicview`}
                                                                                               groupId={this.props.groupId}
                                                                                               topic={topic}
                                                                                               assignment={assignment} />))
                                    }
                                </tbody>
                            );
                        })
                    }
                </Table>
            </div>
        )
    }
}

ConsumerGroupDetailsView.propTypes = {
    groupId: PropTypes.string.isRequired,
    topics: PropTypes.array
};


export default ConsumerGroupDetailsView ;