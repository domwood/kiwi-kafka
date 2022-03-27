import {Button, FormGroup, InputGroup, InputGroupText, Label} from "reactstrap";

import React, {Component} from "react";
import {MdRefresh} from "react-icons/md";
import {Typeahead} from 'react-bootstrap-typeahead';
import "../../App.css";
import {AppDataContext} from "../../contexts/AppDataContext";

class TopicInput extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);

        this.state = {};
    }

    render() {
        return (
            <FormGroup>
                <Label for="topic">Topic:</Label>

                <InputGroup>
                    <Typeahead
                        defaultInputValue={this.context.targetTopic}
                        id={"topicInput"}
                        onChange={select => this.context.setTargetTopic(select[0]) || ''}
                        onInputChange={i => this.context.setTargetTopic(i || '')}
                        options={this.context.topicList}
                        className={"StretchedInput"}
                    />

                    <InputGroupText>
                        <Button color="primary" onClick={this.context.topicListRefresh}>Refresh
                            Topics<MdRefresh/></Button>
                    </InputGroupText>
                </InputGroup>
            </FormGroup>
        )
    }
}

TopicInput.propTypes = {};

export default TopicInput;