import React, { Component } from "react";
import PropTypes from "prop-types";
import {Button, Progress} from "reactstrap";
import {toast} from "react-toastify";
import WebSocketService from "../../../services/WebSocketService";
import ConsumerSlider from "./ConsumerSlider";

class MessageReader extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            consuming: false,
            consumeCount:0,
            totalRecords: 0,
            position:0,
            startValue: 0,
            endValue: 0,
            consumerPosition: 0,
            startingPosition: 0.0
        }
    }

    componentWillMount() {
        WebSocketService.connect(() => false);
    }

    componentWillUnmount() {
        WebSocketService.disconnect();
    }

    clearCounts = (cb, consuming) => {
        this.setState({
            consuming: consuming || false,
            consumeCount:0,
            totalRecords: 0,
            position:0,
            startValue: 0,
            endValue: 0,
            consumerPosition: 0,
            skippedPosition:0
        },cb)
    };

    onWebSocketMessage = (response) => {
        if(this.state.consuming){
            let messages;
            if(this.props.isReversed){
                messages = response.messages
                    .reverse()
                    .concat(this.props.messages)
                    .slice(0, this.props.messageLimit);
            }
            else{
                messages = this.props.messages
                    .concat(response.messages)
                    .slice(this.state.messages.length + response.messages.length > this.props.messageLimit ?  -this.props.messageLimit : 0);
            }
            this.props.updateMessages(messages)
            let position = (response.position||{})

            this.setState({
                consuming: true,
                consumeCount: this.state.consumeCount+response.messages.length,
                position: position.percentage - (position.skippedPercentage||0) || 0,
                totalRecords: position.totalRecords || 0,
                startValue: position.startValue || 0,
                endValue: position.endValue || 0,
                consumerPosition: position.consumerPosition ||0,
                skippedPosition: position.skippedPercentage || 0
            });
        }
    };

    onWebsocketError = (error) => {
        toast.error(`Failed to retrieve data from server ${error.message}`)
        this.clearCounts();
        WebSocketService.disconnect();
    };

    onWebSocketClose = () => {
        this.clearCounts();
        toast.warn("Consumer connection closed");
    };

    startConsumer = () => {
        if(!this.props.targetTopic){
            toast.error("Consumer cannot be started, no topic specified");
            return;
        }

        WebSocketService.connect(() => {
            this.clearCounts(() => {
                this.props.updateMessages([]);

                WebSocketService.consume(
                    [this.props.targetTopic],
                    this.props.filters,
                    this.state.startingPosition,
                    this.onWebSocketMessage,
                    this.onWebsocketError,
                    this.onWebSocketClose
                );
            }, true);
        });
    };

    stopConsumer = () => {
        this.clearCounts(() => WebSocketService.disconnect());
    };

    onUpdateConsumerPosition = (value) => {
        this.setState({
            startingPosition: value
        })
    };

    render() {
        return (
            <div>
                {
                    !this.state.consuming ?
                        <div>
                            <Button onClick={this.startConsumer}
                                    color={"success"}
                                    id="consumeViaWebSocketButton"
                                    disabled={!this.props.targetTopic || this.props.targetTopic.length === 0}
                                    block>Read</Button>

                            {this.state.consumeCount > 0 ? <span>Limited to {this.props.messages.length} of {this.state.consumeCount} consumed </span> :null}
                        </div>
                        :
                        <div>
                            <Button color="warning" onClick={this.stopConsumer} id="consumeViaWebSocketButton" block>
                                Stop Reading
                            </Button>
                        </div>
                }

                <div>
                    <div className={"Gap"}></div>

                    <ConsumerSlider id={'slider'}
                                    consumerPosition={this.state.startingPosition}
                                    onUpdateConsumerPosition={this.onUpdateConsumerPosition}
                                    isConsuming={this.state.consuming}
                    />

                    <div className={"Gap"}></div>

                    <div className="text-center">{
                        this.state.consuming ?
                            'Showing: ' +  this.props.messages.length +
                            ', Matched: '+this.state.consumeCount +
                            ', Records Processed: '+ this.state.totalRecords +
                            ', Offset: '+ (this.state.consumerPosition)+' of ' +  (this.state.endValue) +
                            (this.state.skippedPosition < 0.1 ? '' : ' ('+this.state.skippedPosition+'% Skipped)'): ''
                    }</div>
                    <Progress multi max={100}>
                        <Progress animated bar color="danger" value={this.state.skippedPosition} />
                        <Progress animated bar color="success" value={this.state.position} />
                    </Progress>

                </div>
            </div>
        )
    }
}

MessageReader.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    filters: PropTypes.array.isRequired,
    targetTopic: PropTypes.string,
    messageLimit: PropTypes.number.isRequired,
    messageFromEnd: PropTypes.bool.isRequired,
    isReversed: PropTypes.bool.isRequired,
    updateMessages: PropTypes.func.isRequired,
    messages: PropTypes.array.isRequired
};

export default MessageReader;