import React, {Component} from "react";
import PropTypes from "prop-types";
import {Tooltip} from "reactstrap";
import {MdHelpOutline} from "react-icons/md";

class ProfileToggleToolTip extends Component {

    constructor(props) {
        super(props);

        this.state = {
            disabledToolTip: false
        };
    }

    componentDidMount() {
        this.mounted = true;
    }

    componentWillUnmount() {
        this.mounted = false;
    }

    closeToolTip = () => {
        if(this.mounted){
            this.setState({
                disabledToolTip: !this.state.disabledToolTip
            })
        }
    };

    isProfileDisabled = () => {
        let profiles = this.props.profiles||[];
        return profiles.length !== 0 && profiles.indexOf(this.props.targetProfile) === -1;
    };

    render() {
        return (
            this.isProfileDisabled() ?
                <span style={this.props.style || {}}>
                        <span id={"questionMark"+this.props.id}><MdHelpOutline /> </span>
                        <Tooltip placement={this.props.placement || "right"}
                                 isOpen={this.state.disabledToolTip}
                                 target={"questionMark"+this.props.id}
                                 toggle={this.closeToolTip}>
                            {this.isProfileDisabled() ? '[Disabled] To enable restart kiwi with '+this.props.targetProfile+' profile' : this.props.alternative}
                        </Tooltip>
                    </span>
                : null
        )
    }
}

ProfileToggleToolTip.propTypes = {
    id: PropTypes.string.isRequired,
    profiles: PropTypes.array.isRequired,
    target: PropTypes.object,
    targetProfile: PropTypes.string.isRequired,
    alternative: PropTypes.string,
    placement: PropTypes.string,
    style: PropTypes.object
};

export default ProfileToggleToolTip;