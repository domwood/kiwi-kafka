import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from "reactstrap";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import {MdWarning} from "react-icons/md";
import ProfileToggleToolTip from "../../common/ProfileToggleToolTip";

class DeleteConsumerGroup extends Component {
    constructor(props) {
        super(props);
        this.state = {
            modal: false,
            profileModal: false
        }
    }

    isDeleteDisabled = () => {
        let profiles = this.props.profiles||[];
        return profiles.length !== 0 && profiles.indexOf("write-admin") === -1;
    };

    open = () => {
        if(this.isDeleteEnabled()){
            this.setState({
                modal: true
            })
        }
        else{
            this.setState({
                profileModal: true
            });
        }
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
                <Button id={"deleteGroupId"+this.props.groupId} color="danger" onClick={() => this.open()} disabled={this.isDeleteDisabled()}>Delete Consumer Group <MdWarning /></Button>


                <ProfileToggleToolTip profiles={this.props.profiles}
                                      target={"deleteGroupId"+this.props.groupId}
                                      targetProfile={"admin-write"}
                                      alternative={"Delete consumer group (confirm dialog will open)"}/>

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
    onComplete: PropTypes.func.isRequired,
    profiles: PropTypes.array
};


export default DeleteConsumerGroup;