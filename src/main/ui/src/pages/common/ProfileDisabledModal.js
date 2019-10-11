import React, {Component} from "react";
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from "reactstrap";
import PropTypes from "prop-types";

class ProfileDisabledModal extends Component {
    render() {
        return (
            <Modal isOpen={this.props.isActive} toggle={this.props.onClose} >
                <ModalHeader toggle={this.props.onClose}>Kafka Mode Disabled</ModalHeader>
                <ModalBody>
                    <p>
                        This function has been disabled on purpose. To enable it start kiwi with '{this.props.profileName}' included
                        in the active profile list.
                    </p>
                </ModalBody>
                <ModalFooter>
                    <Button color="primary" onClick={this.props.onClose}>Ok</Button>
                </ModalFooter>
            </Modal>
        )
    }
}

ProfileDisabledModal.propTypes = {
    profiles: PropTypes.array.isRequired,
    isActive: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    profileName: PropTypes.string.isRequired
};

export default ProfileDisabledModal;