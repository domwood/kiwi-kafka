import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup} from "reactstrap";

class ColumnFilterButtons extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name
        }
    }

    render() {
        return (
            <ButtonGroup className={"FullSpan"}>
                {
                    this.props.buttons.map(button => {
                        return (
                            <Button key={'button_' + button.key} size="sm"
                                    onClick={() => this.props.updater(button.key)}
                                    outline={!this.props.viewState[button.key]}>
                                {button.displayName}
                            </Button>
                        )
                    })
                }
            </ButtonGroup>
        )
    }
}

ColumnFilterButtons.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    buttons: PropTypes.array.isRequired,
    viewState: PropTypes.object.isRequired,
    updater: PropTypes.func.isRequired
};

export default ColumnFilterButtons;