import PropTypes from "prop-types";
import React, {Component} from "react";
import Slider, {Handle} from 'rc-slider';
import {Input, InputGroup, InputGroupAddon} from "reactstrap";

const SliderStyle = {
    width: '85%',
    padding: '11px 0px',
    marginRight: '15px',
    backgroundColor: 'rgba(111, 220, 137, 0.0)'
};

class ConsumerSlider extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id
        }
    }

    onUpdateConsumerPosition = (value) => {
        this.props.onUpdateConsumerPosition(parseFloat(value || "0"));
    };

    handle = (props) => {
        const {dragging, value, ...restProps} = props;
        return (
            <React.Fragment>
                <Handle id="handle"
                        value={value}
                        dragging={dragging.toString()}
                        {...restProps} />
            </React.Fragment>
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
                        disabled={this.props.isConsuming}
                        railStyle={{
                            'height': '16px'
                        }}
                        trackStyle={{
                            'height': '16px',
                            'backgroundColor': '#6fdc89',
                        }}
                        handleStyle={{
                            'height': '24px',
                            'width': '24px',
                            'borderColor': '#28a745',
                            'borderRadius': '0.2rem',
                            'backgroundColor': '#28a745'
                        }}
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
    consumerPosition: PropTypes.number.isRequired,
    isConsuming: PropTypes.bool.isRequired
};

export default ConsumerSlider;