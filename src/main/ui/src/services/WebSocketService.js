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

const websocketDataHandler = (message, messageHandler, errorHandler) => {
    if(message.data){
        try{
            let data = JSON.parse(message.data);
            messageHandler(data);
        }
        catch(error){
            errorHandler({ message: "Failed to parse server websocket message"})
        }
    }
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
        WebSocketService.socket = WebSocketFactory();
        WebSocketService.socket.onopen = () => {
            WebSocketService.open = true;
            cb();
        };
        WebSocketService.socket.onclose = () => {
            WebSocketService.open = false;
        };
    }
};

WebSocketService.consume = (topics, filters, messageHandler, errorHandler, closeHandler) => {
    WebSocketService.socket.onmessage = (message) => websocketDataHandler(message, messageHandler, errorHandler);
    WebSocketService.socket.onerror = errorHandler;
    WebSocketService.socket.onclose = () => {
        WebSocketService.open = false;
        closeHandler();
    };

    WebSocketService.send({
        requestType: ".ConsumerRequest",
        topics: topics,
        limit: -1,
        limitAppliesFromStart: false,
        filters: filters || []
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