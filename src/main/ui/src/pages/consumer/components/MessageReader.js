import React, {Component} from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, Progress} from "reactstrap";
import {toast} from "react-toastify";
import WebSocketService from "../../../services/WebSocketService";
import ConsumerSlider from "./ConsumerSlider";
import ProfileToggleToolTip from "../../common/ProfileToggleToolTip";
import {AppDataContext, CLOSED_STATE, CONSUMING_STATE, PAUSED_STATE} from "../../../contexts/AppDataContext";

class MessageReader extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            consumeCount: 0,
            totalRecords: 0,
            position: 0,
            startValue: 0,
            endValue: 0,
            consumerPosition: 0,
            startingPosition: 0.0
        }
    }

    componentDidMount() {
        this.mounted = true;
    }

    componentWillUnmount() {
        if (!this.isConsumerDisabled()) WebSocketService.disconnect();
        this.context.setConsumingState(CLOSED_STATE);

        this.mounted = false;
    }

    isConsumerDisabled = () => {
        let profiles = this.props.profiles || [];
        return profiles.indexOf("read-consumer") === -1 || profiles.length === 0;
    };

    clearCounts = (cb, newState) => {
        this.context.setConsumingState(newState);
        if (this.mounted) {
            this.setState({
                consumeCount: 0,
                totalRecords: 0,
                position: 0,
                startValue: 0,
                endValue: 0,
                consumerPosition: 0,
                skippedPosition: 0
            }, cb)
        }
    };

    onWebSocketMessage = (response) => {
        if (this.context.consumingState === CONSUMING_STATE && this.mounted) {
            let messages;
            if (this.props.isReversed) {
                messages = response.messages
                    .reverse()
                    .concat(this.props.messages)
                    .slice(0, this.props.messageLimit);
            } else {
                messages = this.props.messages
                    .concat(response.messages)
                    .slice(this.state.messages.length + response.messages.length > this.props.messageLimit ? -this.props.messageLimit : 0);
            }
            this.props.updateMessages(messages);
            let position = (response.position || {});

            this.setState({
                consumeCount: this.state.consumeCount + response.messages.length,
                position: position.percentage - (position.skippedPercentage || 0) || 0,
                totalRecords: position.totalRecords || 0,
                startValue: position.startValue || 0,
                endValue: position.endValue || 0,
                consumerPosition: position.consumerPosition || 0,
                skippedPosition: position.skippedPercentage || 0
            });
        }
    };

    onWebsocketError = (error) => {
        if (this.mounted) {
            this.clearCounts(() => toast.error(`Failed to retrieve data from server ${error.message}`), CLOSED_STATE);
            WebSocketService.disconnect();
        }
    };

    onWebSocketClose = () => {
        if (this.mounted) {
            this.clearCounts(() => toast.warn("Consumer connection closed"), CLOSED_STATE);
        }
    };

    startConsumer = () => {
        if (!this.context.targetTopic) {
            toast.error("Consumer cannot be started, no topic specified");
            return;
        }

        WebSocketService.connect(() => {
            this.clearCounts(() => {
                this.props.updateMessages([]);

                WebSocketService.consume(
                    [this.context.targetTopic],
                    this.props.filters,
                    this.state.startingPosition,
                    this.props.partitions,
                    this.onWebSocketMessage,
                    this.onWebsocketError,
                    this.onWebSocketClose
                );
            }, CONSUMING_STATE);
        }, (err) => toast.error("Failed to connect to server: " + err.message));
    };

    stopConsumer = () => {
        this.clearCounts(() =>
            WebSocketService.disconnect(
                (err) => toast.warn("Failed to cleanly disconnect: " + err.message)), CLOSED_STATE);
    };

    pauseConsumer = () => {
        WebSocketService.sendPauseUpdate(true, (err) => toast.warn("Failed to cleanly pause: " + err.message));
        this.context.setConsumingState(PAUSED_STATE);
    };

    unpauseConsumer = () => {
        WebSocketService.sendPauseUpdate(false, (err) => toast.warn("Failed to cleanly pause: " + err.message));
        this.context.setConsumingState(CONSUMING_STATE);
    };

    onUpdateConsumerPosition = (value) => {
        this.setState({
            startingPosition: value
        });
    };

    renderButton = () => {
        switch (this.context.consumingState) {
            case CONSUMING_STATE:
                return <React.Fragment>
                    <ButtonGroup style={{width: "100%"}}>
                        <Button color="warning" onClick={this.pauseConsumer} id="consumeViaWebSocketButton" block>
                            Pause Reading
                        </Button>
                        <Button color="danger" onClick={this.stopConsumer} id="consumeViaWebSocketButton" block>
                            Stop Reading
                        </Button>
                    </ButtonGroup>
                </React.Fragment>
            case PAUSED_STATE:
                return <React.Fragment>
                    <ButtonGroup style={{width: "100%"}}>
                        <Button color="success" onClick={this.unpauseConsumer} id="consumeViaWebSocketButton" block>
                            Unpause Reading
                        </Button>
                        <Button color="danger" onClick={this.stopConsumer} id="consumeViaWebSocketButton" block>
                            Stop Reading
                        </Button>
                    </ButtonGroup>
                </React.Fragment>
            case CLOSED_STATE:
            default:
                return <React.Fragment>
                    <Button onClick={this.startConsumer}
                            color={"success"}
                            id="consumeViaWebSocketButton"
                            disabled={(!this.context.targetTopic) || this.isConsumerDisabled()}
                            block>Read</Button>
                    <ProfileToggleToolTip profiles={this.props.profiles}
                                          id={`${this.props.topic}_consume`}
                                          targetProfile={"read-consumer"}
                                          placement={"top"}
                                          style={{
                                              "float": "right",
                                              "marginRight": "-20px",
                                              "marginTop": "-31px"
                                          }}
                    />
                    {this.state.consumeCount > 0 ?
                        <span>Limited to {this.props.messages.length} of {this.state.consumeCount} consumed </span> : null}
                </React.Fragment>
        }
    }

    render() {
        return (
            <div>
                {
                    this.renderButton()
                }
                <div>
                    <div className={"Gap"}/>
                    <ConsumerSlider id={'slider'}
                                    consumerPosition={this.state.startingPosition}
                                    onUpdateConsumerPosition={this.onUpdateConsumerPosition}
                                    isConsuming={this.context.consumingState !== CLOSED_STATE}
                    />
                    <div className={"Gap"}/>

                    <div className="text-center">{
                        this.context.consumingState !== CLOSED_STATE ?
                            'Showing: ' + this.props.messages.length +
                            ', Matched: ' + this.state.consumeCount +
                            ', Records Processed: ' + this.state.totalRecords +
                            ', Offset: ' + (this.state.consumerPosition) + ' of ' + (this.state.endValue) +
                            (this.state.skippedPosition < 0.1 ? '' : ' (' + this.state.skippedPosition + '% Skipped)') : ''
                    }</div>
                    <Progress multi max={100}>
                        <Progress animated bar color="danger" value={this.state.skippedPosition | 0}/>
                        <Progress animated bar color="success"
                                  value={(this.state.position > 99.0 ? 100 : this.state.position) | 0}/>
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
    messageLimit: PropTypes.number.isRequired,
    messageFromEnd: PropTypes.bool.isRequired,
    isReversed: PropTypes.bool.isRequired,
    updateMessages: PropTypes.func.isRequired,
    messages: PropTypes.array.isRequired,
    profiles: PropTypes.array.isRequired,
    partitions: PropTypes.array.isRequired,
    topic: PropTypes.string
};

export default MessageReader;