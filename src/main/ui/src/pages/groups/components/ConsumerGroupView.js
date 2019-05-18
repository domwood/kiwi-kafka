import React, {Component} from "react";
import {Button, ListGroupItem} from "reactstrap";
import PropTypes from "prop-types";
import ConsumerGroupDetailsView from "./ConsumerGroupDetailsView";

class ConsumerGroupView extends Component {
    constructor(props) {
        super(props);

        this.state = {
            toggle: false
        }
    }

    toggle = () => {
        this.setState({
            toggle: !this.state.toggle
        })
    };

    render() {
        return (
            <div>
                <ListGroupItem key={this.props.groupId + "_parent"} id={this.props.groupId}>
                    <Button color={this.state.toggle ? "success" : "secondary"} size="sm" onClick={() => this.toggle()} block>{this.props.groupId}</Button>
                    {this.state.toggle ? <ConsumerGroupDetailsView groupId={this.props.groupId}/>  : ''}
                </ListGroupItem>
            </div>
        )
    }
}

ConsumerGroupView.propTypes = {
    groupId: PropTypes.string.isRequired
};


export default ConsumerGroupView ;