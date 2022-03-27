import React, {Component} from "react";
import {Button, ButtonGroup, Container, Spinner} from "reactstrap";
import "../../App.css";
import {MdRefresh} from "react-icons/md";
import CreateTopic from "./components/CreateTopic";
import TopicView from "./components/TopicView";
import SearchableViewList from "../common/SearchableViewList";
import PropTypes from "prop-types";
import {AppDataContext} from "../../contexts/AppDataContext";

class KafkaTopics extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);

        this.state = {
            addTopic: false
        }
    }

    addTopic = (toggle) => {
        this.setState({
            addTopic: toggle
        })
    };

    render() {

        return (
            <Container className={"WideBoi"}>

                <div className="mt-lg-4"/>
                <h1>Kafka Topics</h1>
                <div className="mt-lg-4"/>
                <div className={"TwoGap"}/>

                <ButtonGroup>
                    <Button color="primary" onClick={this.context.topicListRefresh}>Reload List <MdRefresh/></Button>
                    {!this.state.addTopic ?
                        <Button color="success" onClick={() => this.addTopic(true)}>Add Topic +</Button> : ''}
                    {this.context.topicLoading ? <Spinner color="secondary"/> : ''}
                </ButtonGroup>

                <div className={"Gap"}/>

                {this.state.addTopic ? <CreateTopic onClose={() => this.addTopic(false)}
                                                    onCreate={this.context.topicListRefresh}
                                                    profiles={this.props.profiles}/> : ''}

                <div className={"Gap"}/>

                <SearchableViewList id={"topicViewList"}
                                    elementList={this.context.topicList}
                                    elementViewProvider={(topic) => <TopicView id={`${topic}_view`}
                                                                               key={`${topic}_view`}
                                                                               topic={topic}
                                                                               onDeletion={this.context.topicListRefresh}
                                                                               profiles={this.props.profiles}/>}/>
            </Container>
        );
    }
}

KafkaTopics.propTypes = {
    profiles: PropTypes.array.isRequired
};

export default KafkaTopics;