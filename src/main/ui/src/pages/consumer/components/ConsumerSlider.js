import PropTypes from "prop-types";
import React, { Component } from "react";
import Slider, {Handle} from 'rc-slider';
import {Input, InputGroup, InputGroupAddon} from "reactstrap";

const SliderStyle = {
    width: '85%',
    padding: '16px 0px',
    marginRight: '15px'
};

class ConsumerSlider extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id
        }
    }

    onUpdateConsumerPosition = (value) => {
        this.props.onUpdateConsumerPosition(parseFloat(value || 0));
    };


    handle = (props) => {
        const { value, dragging, index, ...restProps } = props;
        return (
            <div>
                <Handle id="handle" value={value} {...restProps} />
            </div>

        );
    };

    render() {
        return (
            <div>
                <InputGroup>
                    <Slider
                        style={SliderStyle}
                        className={"sliderCss"}
                        min={0}
                        max={100}
                        defaultValue={0}
                        step={.1}
                        handle={this.handle}
                        onChange={this.onUpdateConsumerPosition}
                        value={this.props.consumerPosition}
                    />
                    <Input
                        min={0}
                        max={100}
                        type="number"
                        step=".1"
                        value={this.props.consumerPosition}
                        onChange={(event) => this.onUpdateConsumerPosition(event.target.value)}
                    />
                    <InputGroupAddon addonType="append">% Start @</InputGroupAddon>
                </InputGroup>
            </div>
        )
    }
}


ConsumerSlider.propTypes = {
    id: PropTypes.string.isRequired,
    onUpdateConsumerPosition: PropTypes.func.isRequired,
    consumerPosition: PropTypes.number.isRequired
};

export default ConsumerSlider;