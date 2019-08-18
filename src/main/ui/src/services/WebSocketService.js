import WebSocketFactory from "./WebSocketFactory";


//TODO WIP POC
const WebSocketService = {
    socket:{
        readyState: 4, //0 Connecting, 1 Connected, 2 Closing, 3 Closed (∴ 4 =~ UNCREATED)
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
    if(message.data){
        try{
            let data = JSON.parse(message.data);
            messageHandler(data);
        }
        catch(error){
            errorHandler({ message: "Failed to parse server websocket message"})
        }
    }
    ack();
};

WebSocketService.send = (data) => {
    if(data) {
        WebSocketService.pending.push((sock) => sock.send(JSON.stringify(data)));
    }
    if(WebSocketService.socket.readyState === 0) {
        setTimeout(() => WebSocketService.send(data), 50);
    }
    else if(WebSocketService.socket.readyState === 1){
        while(WebSocketService.pending.length > 0 && WebSocketService.socket.readyState === 1) WebSocketService.pending.pop()(WebSocketService.socket);
    }
    else{
        WebSocketService.connect(() => WebSocketService.send(data));
    }
};

WebSocketService.connect = (cb) => {
    if(WebSocketService.socket.readyState > 1){
        console.log("Opening new websocket")

        WebSocketService.socket = WebSocketFactory();
        WebSocketService.socket.onopen = () => {
            WebSocketService.open = true;
            cb();
        };
        WebSocketService.socket.onclose = () => {
            WebSocketService.open = false;
        };
    }
    cb();
};

WebSocketService.consume = (topics, filters, startPosition, messageHandler, errorHandler, closeHandler) => {
    WebSocketService.socket.onmessage = (message) => websocketDataHandler(message, messageHandler, errorHandler, websocketAck(WebSocketService.send));
    WebSocketService.socket.onerror = errorHandler;
    WebSocketService.socket.onclose = () => {
        WebSocketService.open = false;
        closeHandler();
    };

    WebSocketService.send({
        requestType: ".ConsumerRequest",
        topics: topics,
        limit: -1,
        filters: filters || [],
        consumerStartPosition : startPosition < 0.1 ? null : {topicPercentage: startPosition}
    });
};

WebSocketService.disconnect = () => {

    WebSocketService.send({
        requestType: ".CloseTaskRequest"
    });
    if(WebSocketService.socket.readyState > 2){
        WebSocketService.open = true;
        WebSocketService.socket.close();
    }
};

export default WebSocketService;