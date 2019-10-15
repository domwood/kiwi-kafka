import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, Modal, ModalBody, ModalFooter, ModalHeader, Tooltip} from "reactstrap";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";

class DeleteTopic extends Component {
    constructor(props) {
        super(props);
        this.state = {
            modal: false,
            profileModal: false,
            disabledToolTip: false
        }
    }

    isDeleteDisabled = () => {
        let profiles = this.props.profiles||[];
        return profiles.length !== 0 && profiles.indexOf("write-admin") === -1;
    };

    open = () => {
        this.setState({
            modal: true
        });
    };

    close = () => {
        this.setState({
            modal: false
        })
    };

    deleteTopic = () => {
        ApiService.deleteTopic(this.props.topic, () =>{
            toast.success("Topic Deletion Successful");
            this.setState({
                modal: false
            }, this.props.onComplete)
        }, (err) => toast.error(`Failed to delete topic ${err.message}`))
    };

    closeToolTip = () => {
        this.setState({
            disabledToolTip: !this.state.disabledToolTip
        })
    };

    render() {
        return (
            <div>
                <Button id={"DeleteTopic"+this.props.topic} color="danger" onClick={() => this.open()} disabled={this.isDeleteDisabled}>Delete Topic</Button>

                <Tooltip placement="right" isOpen={this.state.disabledToolTip} target={"DeleteTopic"+this.props.topic} toggle={this.closeToolTip}>
                    {this.isDeleteDisabled() ? '[Disabled] To enable restart kiwi with admin-write profile' : 'Delete the topic (confirmation dialog will open)'}
                </Tooltip>

                <Modal isOpen={this.state.modal} toggle={this.close} >
                    <ModalHeader toggle={this.close}>Delete Kafka Topic</ModalHeader>
                    <ModalBody>
                        <p>
                            Note: This will only have an effect if the broker has <i>delete.topic.enable</i> set to true.
                        </p>
                        <p>
                            <b>
                                Deleting the topic will lead to a loss of all data on that topic. Are you sure?
                            </b>
                        </p>
                    </ModalBody>
                    <ModalFooter>
                        <Button color="danger" onClick={this.deleteTopic}>Yes</Button>{' '}
                        <Button color="success" onClick={this.close}>Cancel</Button>
                    </ModalFooter>
                </Modal>

            </div>
        )
    }
}

DeleteTopic.propTypes = {
    topic: PropTypes.string.isRequired,
    onComplete: PropTypes.func.isRequired,
    profiles: PropTypes.array.isRequired
};


export default DeleteTopic;