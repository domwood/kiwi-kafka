import * as ApiService from "../../services/ApiService";

import {Button, FormGroup, InputGroup, InputGroupText, Label} from "reactstrap";

import React, {Component} from "react";
import {MdRefresh} from "react-icons/md";
import PropTypes from "prop-types";
import {toast} from "react-toastify";
import {Typeahead} from 'react-bootstrap-typeahead';
import "../../App.css";

class TopicInput extends Component {

    constructor(props) {
        super(props);

        this.state = {
            topicList: []
        };

        this._cancellable = false;
    }

    componentDidMount() {
        this._cancellable = true;
        this._cancellable && this.getTopicList();
    }

    componentWillUnmount() {
        this._cancellable = false;
    }

    getTopicList = (reload) => {
        if (this.state.topicList.length === 0 || reload) {
            ApiService.getTopics((topics) => {
                if (this._cancellable) {
                    this.setState({
                        topicList: topics
                    });
                }
            }, () => toast.warn("Could not retrieve topic list from server"));
        }
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
                        onInputChange={i => this.setTargetTopic(i || '')}
                        options={this.state.topicList}
                        className={"StretchedInput"}
                    />

                    <InputGroupText>
                        <Button color="primary" onClick={() => this.getTopicList(true)}>Refresh
                            Topics<MdRefresh/></Button>
                    </InputGroupText>
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