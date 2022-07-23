import WebSocketFactory from "./WebSocketFactory";
import SessionStore from "./SessionStore";

const CONNECTING = 0;
const CONNECTED = 1;
// eslint-disable-next-line no-unused-vars
const CLOSING = 2;
const CLOSED = 3;

const WebSocketService = {
    socket: {
        readyState: CLOSED, //0 Connecting, 1 Connected, 2 Closing, 3 Closed)
        send: () => '',
        close: () => ''
    },
    pending: [],
    open: false
};

const websocketAck = (sender) => {
    return () => sender({
        requestType: ".MessageAcknowledge",
    });
};

const websocketDataHandler = (message, messageHandler, errorHandler, ack) => {
    if (message.data) {
        try {
            let data = JSON.parse(message.data);
            messageHandler(data);
        } catch (error) {
            errorHandler({message: "Failed to parse server websocket message"})
        }
    }
    ack();
};

WebSocketService.send = (data, cb, eb) => {
    if (data) {
        WebSocketService.pending.push((sock) => sock.send(JSON.stringify(data)));
    }
    if (WebSocketService.socket.readyState === CONNECTING) {
        setTimeout(() => WebSocketService.send(data, eb), 50);
    } else if (WebSocketService.socket.readyState === 1) {
        while (WebSocketService.pending.length > 0 && WebSocketService.socket.readyState === 1) WebSocketService.pending.pop()(WebSocketService.socket);
    } else {
        WebSocketService.connect(() => WebSocketService.send(data), eb);
    }
};

WebSocketService.connect = (cb, eb) => {
    if (WebSocketService.socket.readyState > CONNECTED) {

        WebSocketService.socket = WebSocketFactory();
        WebSocketService.socket.onopen = () => {
            WebSocketService.open = true;
            cb();
        };
        WebSocketService.socket.onclose = () => {
            WebSocketService.open = false;
        };
        WebSocketService.socket.onerror = (err) => {
            if (eb) eb(err);
        }
    }
    cb();
};

WebSocketService.consume = (topics, filters, startPosition, partitions, messageHandler, errorHandler, closeHandler) => {
    WebSocketService.socket.onmessage = (message) => websocketDataHandler(message, messageHandler, errorHandler, websocketAck(WebSocketService.send));
    WebSocketService.socket.onerror = errorHandler;
    WebSocketService.socket.onclose = () => {
        WebSocketService.open = false;
        closeHandler();
    };

    SessionStore.getActiveCluster((activeCluster) => {
        WebSocketService.send({
            clusterName: activeCluster,
            requestType: ".ConsumerRequest",
            topics: topics,
            limit: -1,
            filters: filters || [],
            consumerStartPosition: startPosition < 0.1 ? {partitions: partitions} : {
                topicPercentage: startPosition,
                partitions: partitions
            }
        }, errorHandler);
    }, errorHandler)

};

WebSocketService.disconnect = (errorHandler) => {
    SessionStore.getActiveCluster((activeCluster) => {
        WebSocketService.send({
            clusterName: activeCluster,
            requestType: ".CloseTaskRequest"
        }, errorHandler);
        if (WebSocketService.socket.readyState > CONNECTED) {
            WebSocketService.open = true;
            WebSocketService.socket.close();
        }
    }, errorHandler);

};

WebSocketService.sendPauseUpdate = (paused, eb) => {
    SessionStore.getActiveCluster((activeCluster) => {
        WebSocketService.send({
            clusterName: activeCluster,
            requestType: ".PauseTaskRequest",
            pauseSession: paused
        }, eb);
    }, eb);
};

export default WebSocketService;