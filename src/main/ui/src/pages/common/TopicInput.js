import * as ApiService from "../../services/ApiService";

import {
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    FormGroup,
    InputGroup,
    InputGroupButtonDropdown,
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
                if(topics.length > 0){
                    this.setTargetTopic(topics[0])
                }
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
        this.props.onUpdate(topic);
    };

    render() {
        return (
            <FormGroup>
                <Label for="topic">Topic:</Label>

                <InputGroup>

                    <Typeahead
                        id={"topicInput"}
                        onChange={selected => selected ? this.setTargetTopic(selected[0]) : ''}
                        options={this.state.topicList}
                        className={"StretchedInput"}
                    />

                    <InputGroupButtonDropdown addonType="append" isOpen={this.state.dropdownOpen} toggle={this.toggleDropDown}>

                        <DropdownToggle caret>
                            Topic List
                        </DropdownToggle>
                        <DropdownMenu
                            modifiers={{
                                setMaxHeight: {
                                    enabled: true,
                                    order: 890,
                                    fn: (data) => {
                                        return {
                                            ...data,
                                            styles: {
                                                ...data.styles,
                                                overflow: 'auto',
                                                maxHeight: 500,
                                            },
                                        };
                                    },
                                },
                            }}>


                            {
                                this.state.topicList.map(topic =>
                                    <DropdownItem key={topic} name={topic} onClick={() => this.setTargetTopic(topic)}>{topic}</DropdownItem>)
                            }
                            <DropdownItem divider />
                            <DropdownItem onClick={() => this.getTopicList(true)} >
                                <MdRefresh /> Reload Topics
                            </DropdownItem>
                        </DropdownMenu>

                    </InputGroupButtonDropdown>
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