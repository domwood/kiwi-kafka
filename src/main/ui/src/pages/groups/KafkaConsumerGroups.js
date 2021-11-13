import React, {Component} from "react";
import {
    Button,
    Container,
    Spinner
} from "reactstrap";
import {MdRefresh} from "react-icons/md";
import * as ApiService from "../../services/ApiService";
import {toast} from "react-toastify";
import SearchableViewList from "../common/SearchableViewList";
import ConsumerGroupView from "./components/ConsumerGroupView";
import PropTypes from "prop-types";

class KafkaConsumerGroups extends Component {

    constructor(props) {
        super(props);

        this.state = {
            groupList: [],
            loading: false,
            ignoreKiwiConsumers: true
        };
    }

    componentDidMount() {
        this.mounted = true;
        this.loadConsumerGroups();
    }

    componentWillUnmount(){
        this.mounted = false;
    }

    loadConsumerGroups = () => {
        this.setState({
            loading: true
        }, () => {
            ApiService.getConsumerGroups((groups) => {
                if(this.mounted){
                    this.setState({
                        groupList:  groups || [],
                        loading:false
                    }, () => {
                        toast.info("Refreshed consumer group list from server");
                    });
                }
            }, () => {
                if(this.mounted){
                    this.setState({
                        loading: false
                    });
                    toast.error("Could not retrieve consumer group list from server")
                }
            });
        });
    };

    render() {
        return (
            <Container className={"WideBoi"}>

                <div className="mt-lg-4"/>
                <h1>Kafka Consumer Groups</h1>
                <div className="mt-lg-4"/>
                <div className={"TwoGap"}/>


                <Button color="primary" onClick={this.loadConsumerGroups}>Reload List <MdRefresh/></Button>

                <div className={"Gap"}/>
                {this.state.loading ? <Spinner color="secondary"/> : ''}

                <div className={"Gap"}/>

                <SearchableViewList elementList={this.state.groupList}
                                    elementViewProvider={(group) => <ConsumerGroupView key={`${group}_search`} groupId={group} onDeletion={this.loadConsumerGroups} profiles={this.props.profiles}/> } />
            </Container>
        );
    }

}

KafkaConsumerGroups.propTypes = {
    profiles: PropTypes.array.isRequired
};

export default KafkaConsumerGroups;