import React, {Component} from "react";
import PropTypes from "prop-types";
import {Tooltip} from "reactstrap";

class ProfileToggleToolTip extends Component {

    constructor(props) {
        super(props);

        this.state = {
            disabledToolTip: false
        };
    }

    closeToolTip = () => {
        this.setState({
            disabledToolTip: !this.state.disabledToolTip
        })
    };

    isProfileDisabled = () => {
        let profiles = this.props.profiles||[];
        return profiles.length !== 0 && profiles.indexOf(this.props.targetProfile) === -1;
    };

    render() {
        return (
            <Tooltip placement="right" isOpen={this.state.disabledToolTip} target={this.props.target} toggle={this.closeToolTip}>
                {this.isProfileDisabled() ? '[Disabled] To enable restart kiwi with '+this.props.targetProfile+' profile' : this.props.alternative}
            </Tooltip>
        )
    }
}

ProfileToggleToolTip.propTypes = {
    profiles: PropTypes.array.isRequired,
    target: PropTypes.string.isRequired,
    targetProfile: PropTypes.string.isRequired,
    alternative: PropTypes.string
};

export default ProfileToggleToolTip;