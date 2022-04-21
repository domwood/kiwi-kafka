import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, InputGroupText, Spinner, Table} from "reactstrap";
import {MdRefresh} from "react-icons/md";
import {toast} from "react-toastify";
import * as ApiService from "../../../services/ApiService";
import ConsumerGroupTopicDetailsView from "./ConsumerGroupTopicDetailsView";
import DeleteConsumerGroup from "./DeleteConsumerGroup";

class ConsumerGroupDetailsView extends Component {
    constructor(props) {
        super(props);

        this.state = {
            groupData: {},
            loading: false,
            detailsShown: false
        }
    }

    componentDidMount() {
        this.mounted = true;
    }

    componentWillUnmount() {
        this.mounted = false;
    }

    loadConsumerDetails = () => {
        this.setState({
            loading: true,
            detailsShown: true
        }, this.getConsumerGroupDetails);
    };

    getConsumerGroupDetails = () => {
        this.setState({
            detailsShown: true
        })
        ApiService.getConsumerGroupDetailsWithOffsets(this.props.groupId, (data) => {
            if (this.mounted) {
                this.setState({
                    groupData: data,
                    loading: false
                }, () => toast.info(`Retrieved data for groupId ${this.props.groupId}`));
            }
        }, (err) => {
            this.setState({loading: false});
            toast.error(`Error retreiving ${this.props.groupId} group info: ${err.message}`)
        })
    };

    render() {
        return (
            <div >
                <hr/>
                <ButtonGroup>
                    <InputGroupText className={"input-group-text-padded"} style={{minWidth: "500px", border: "none"}}><b>Consumer
                        Group: {this.props.groupId}</b></InputGroupText>
                    {
                        !this.state.detailsShown ?
                            <Button color="primary" onClick={this.loadConsumerDetails}>Show
                                Details</Button> :
                            <React.Fragment>
                                <Button color="primary" onClick={this.loadConsumerDetails}>Refresh <MdRefresh/></Button>
                                <DeleteConsumerGroup onComplete={this.props.onDeletion} groupId={this.props.groupId}
                                                     profiles={this.props.profiles}/>
                            </React.Fragment>
                    }
                </ButtonGroup>

                {this.state.loading ? <Spinner color="secondary"/> : ''}

                <div className={"Gap"}/>
                {Object.entries(this.state.groupData).length > 0 ?
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
                            .filter(([topic]) => !this.props.topics || this.props.topics.includes(topic))
                            .map(([topic, data]) => {
                                return (
                                    <tbody key={`${encodeURIComponent(this.props.groupId)}_${topic}`}>
                                    {(this.props.topics || []).length !== 1 ?
                                        <tr className="table-primary" key={`${topic}_header_row`}>
                                            <td colSpan="8" style={{"textAlign": "center"}}>Topic: {topic}</td>
                                        </tr> : <tr className="table-primary" key={`${topic}_header_row`}/>
                                    }
                                    {
                                        data.map(assignment => (<ConsumerGroupTopicDetailsView
                                            key={`${encodeURIComponent(this.props.groupId)}_${assignment.partition}_topicview`}
                                            groupId={this.props.groupId}
                                            topic={topic}
                                            assignment={assignment}/>))
                                    }
                                    </tbody>
                                );
                            })
                        }
                    </Table>
                    : !this.state.loading && this.state.detailsShown ?
                        <Table>
                            <tbody>
                            <tr className="table-primary" key={`${this.props.groupId}_inactive`}>
                                <td colSpan="8" style={{"textAlign": "center"}}>No current assignment
                                    for: {this.props.groupId}</td>
                            </tr>
                            </tbody>
                        </Table> : <React.Fragment />
                }
            </div>
        )
    }
}

ConsumerGroupDetailsView.propTypes = {
    groupId: PropTypes.string.isRequired,
    topics: PropTypes.array,
    onDeletion: PropTypes.func.isRequired,
    profiles: PropTypes.array.isRequired
};


export default ConsumerGroupDetailsView;