import * as ApiService from "../../services/ApiService";

import {
    Button,
    FormGroup,
    InputGroup, InputGroupAddon,
    Label
} from "reactstrap";

import React, {Component} from "react";
import { MdRefresh } from "react-icons/md/index";
import PropTypes from "prop-types";
import {toast} from "react-toastify";
import {Typeahead} from 'react-bootstrap-typeahead';
import "../../App.css";

class TopicInput extends Component {

    constructor(props) {
        super(props);

        this.state = {
            topicList: []
        }
    }

    componentDidMount(){
        this.getTopicList();
    }

    getTopicList = (reload) => {
        if(this.state.topicList.length === 0 || reload){
            ApiService.getTopics((topics) => {
                this.setState({
                    topicList:topics
                });
            }, () => toast.warn("Could not retrieve topic list from server"));
        }
    };

    toggleDropDown = () => {
        this.getTopicList();
        this.setState({
            dropdownOpen: !this.state.dropdownOpen
        });
    };

    setTargetTopic = (topic) => {
        this.props.onUpdate(topic || '');
    };

    render() {
        return (
            <FormGroup>
                <Label for="topic">Topic:</Label>

                <InputGroup>

                    <Typeahead
                        defaultInputValue={this.props.targetTopic}
                        id={"topicInput"}
                        onChange={select => this.setTargetTopic(select[0]) || ''}
                        onInputChange={i =>this.setTargetTopic(i || '') }
                        options={this.state.topicList}
                        className={"StretchedInput"}
                        selectHintOnEnter={true}
                    />

                    <InputGroupAddon addonType="append">
                        <Button color="primary" onClick={() => this.getTopicList(true)}>Refresh Topics<MdRefresh/></Button>
                    </InputGroupAddon>
                </InputGroup>
            </FormGroup>
        )
    }
}

TopicInput.propTypes = {
    onUpdate: PropTypes.func.isRequired,
    targetTopic: PropTypes.string.isRequired
};

export default TopicInput;