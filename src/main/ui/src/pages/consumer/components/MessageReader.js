import React, { Component } from "react";
import PropTypes from "prop-types";
import {Button, ButtonGroup, Spinner} from "reactstrap";
import * as ApiService from "../../../services/ApiService";
import {toast} from "react-toastify";
import WebSocketService from "../../../services/WebSocketService";

class MessageReader extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name
        }
    }

    componentWillMount() {
        WebSocketService.connect(() => false);
    }

    componentWillUnmount() {
        WebSocketService.disconnect();
    }

    onWebSocketMessage = (response) => {
        if(this.state.continuous){
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
            this.setState({
                continuous: true,
                consumeCount: this.state.consumeCount+response.messages.length,
            });
        }
    };

    onWebsocketError = (error) => {
        toast.error(`Failed to retrieve data from server ${error.message}`)
        this.setState({continuous: false});
        WebSocketService.disconnect();
    };

    onWebSocketClose = () => {
        this.setState({continuous: false});
        toast.info("Consumer connection closed");
    };

    onRestResponse = (response) => {
        this.setState({
            consuming: false,
        }, () => {
            this.props.updateMessages(this.props.isReversed? response.messages.reverse() : response.messages);
            toast.info(`Retrieved ${response.messages.length} records from ${this.state.targetTopic}`)
        })
    };

    onRestError = (error) => {
        this.setState({
            consuming: false
        });
        toast.error(`Failed to retrieve data from server ${error.message}`)
    };

    getKafkaMessage = () => {
        if(!this.props.targetTopic){
            toast.error("Consumer cannot be started, no topic specified");
            return;
        }

        this.setState({
            consuming: true
        }, () => {
            ApiService.consume(
                [this.props.targetTopic],
                this.props.messageLimit,
                this.props.messageFromEnd,
                this.props.filters,
                this.onRestResponse,
                this.onRestError)
        })
    };

    startConsumer = () => {
        if(!this.props.targetTopic){
            toast.error("Consumer cannot be started, no topic specified");
            return;
        }

        this.setState({
            continuous: true,
            consumeCount:0
        });

        this.props.updateMessages([]);

        WebSocketService.consume(
            [this.props.targetTopic],
            this.props.filters,
            this.onWebSocketMessage,
            this.onWebsocketError,
            this.onWebSocketClose
        );
    };

    stopConsumer = () => {
        this.setState({
            continuous: false
        }, () => WebSocketService.disconnect());
    };

    render() {
        return (
            <div>
                {
                    !this.state.continuous ?
                        <div>
                            <ButtonGroup>
                                <Button onClick={this.getKafkaMessage} id="consumeViaRestButton">Read and Close</Button>
                                <Button onClick={this.startConsumer} id="consumeViaWebSocketButton">Read Continuously</Button>
                            </ButtonGroup>
                        </div>
                        :
                        <div>
                            <ButtonGroup>
                                <Button onClick={this.getKafkaMessage} disabled={true} id="consumeViaRestButton">Read and Close</Button>
                                <Button color="warning" onClick={this.stopConsumer} id="consumeViaWebSocketButton">Stop Reading</Button>
                            </ButtonGroup>
                            <span>Limited to {this.props.messages.length} of {this.state.consumeCount} consumed </span>
                            <Spinner color="secondary" />
                        </div>
                }
                {this.state.consuming ? <Spinner color="secondary" /> : ''}
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