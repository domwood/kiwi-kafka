import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from "reactstrap";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import {MdWarning} from "react-icons/md";

class DeleteConsumerGroup extends Component {
    constructor(props) {
        super(props);
        this.state = {
            modal: false
        }
    }
    open = () => {
        this.setState({
            modal: true
        })
    };

    close = () => {
        this.setState({
            modal: false
        })
    };

    deleteConsumerGroup = () => {
        ApiService.deleteConsumerGroup(this.props.groupId, () => {
            toast.info(`Deleted consumer group ${this.props.groupId}`);
            this.setState({
                modal: false
            }, this.props.onComplete)
        }, err => toast.error(`Failed to delete consumer group ${err.message}`))
    };

    render() {
        return (
            <span>
                <Button color="danger" onClick={() => this.open()}>Delete Consumer Group <MdWarning /></Button>

                <Modal isOpen={this.state.modal} toggle={this.close} >
                    <ModalHeader toggle={this.close}>Delete Consumer Group</ModalHeader>
                    <ModalBody>
                        <p>
                            Note: This action only works for consumer groups without active assignments
                        </p>
                        <p>
                            <b>
                                Deleting the Consumer Group may lead to loss or duplication of consumed data. Are you sure?
                            </b>
                        </p>
                    </ModalBody>
                    <ModalFooter>
                        <Button color="danger" onClick={this.deleteConsumerGroup}>Yes</Button>{' '}
                        <Button color="success" onClick={this.close}>Cancel</Button>
                    </ModalFooter>
                </Modal>

            </span>
        )
    }
}

DeleteConsumerGroup.propTypes = {
    groupId: PropTypes.string.isRequired,
    onComplete: PropTypes.func.isRequired
};


export default DeleteConsumerGroup;