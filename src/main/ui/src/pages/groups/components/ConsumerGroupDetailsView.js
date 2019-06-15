import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, InputGroupAddon, InputGroupText, Spinner, Table} from "reactstrap";
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
            loading: true
        }
    }

    componentDidMount() {
        this.mounted = true;
        this.loadConsumerDetails();
    }

    componentWillUnmount() {
        this.mounted = false;
    }

    loadConsumerDetails = () => {
        this.setState({
            loading:true
        }, this.getConsumerGroupDetails);
    };

    getConsumerGroupDetails = () => {
        ApiService.getConsumerGroupDetailsWithOffsets(this.props.groupId, (data) => {
            if(this.mounted){
                this.setState({
                    groupData:data,
                    loading: false
                }, () => toast.info(`Retrieved data for groupId ${this.props.groupId}`));
            }
        }, (err) => {
            this.setState({loading:false});
            toast.error(`Error retreiving ${this.props.groupId} group info: ${err.message}`)
        })
    };

    render() {
        return (
            <div>
                <div className={"TwoGap"} />
                <ButtonGroup>
                    <InputGroupAddon addonType="prepend">
                        <InputGroupText>Consumer Group: {this.props.groupId}</InputGroupText>
                    </InputGroupAddon>
                    <Button color="primary" onClick={this.loadConsumerDetails}>Refresh <MdRefresh/></Button>
                    {/*<Button color="warning" disabled>Reset to Latest</Button>*/}
                    {/*<Button color="warning" disabled>Reset to Earliest</Button>*/}
                    <DeleteConsumerGroup onComplete={this.props.onDeletion} groupId={this.props.groupId}/>
                </ButtonGroup>


                {this.state.loading ? <Spinner color="secondary"/> : ''}

                <div className={"Gap"} />
                { Object.entries(this.state.groupData).length > 0 ?
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


                        { Object.entries(this.state.groupData)
                            .filter(([topic, data]) => !this.props.topics || this.props.topics.includes(topic))
                            .map(([topic, data]) => {
                                return (
                                    <tbody key={`${this.props.groupId}_${topic}`}>
                                    {(this.props.topics || []).length !== 1 ?
                                        <tr className="table-primary" key={`${topic}_header_row`}>
                                            <td colSpan="8" style={{"textAlign": "center"}}>{topic}</td>
                                        </tr> : <tr className="table-primary" key={`${topic}_header_row`}/>
                                    }
                                    {
                                        data.map(assignment => (<ConsumerGroupTopicDetailsView
                                            key={`${this.props.groupId}_${assignment.partition}_topicview`}
                                            groupId={this.props.groupId}
                                            topic={topic}
                                            assignment={assignment}/>))
                                    }
                                    </tbody>
                                );
                            })
                        }
                    </Table> : !this.state.loading ? <Table>
                        <tr className="table-primary" key={`${this.props.groupId}_inactive`}>
                            <td colSpan="8" style={{"textAlign":"center"}}>No current assignment for: {this.props.groupId}</td>
                        </tr>
                    </Table> : ''
                }
            </div>
        )
    }
}

ConsumerGroupDetailsView.propTypes = {
    groupId: PropTypes.string.isRequired,
    topics: PropTypes.array,
    onDeletion: PropTypes.func.isRequired
};


export default ConsumerGroupDetailsView ;