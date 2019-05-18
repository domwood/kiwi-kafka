import WebSocketFactory from "./WebSocketFactory";

//TODO WIP POC
const client = {
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

client.send = (data) => {
    if(data) {
        client.pending.push((sock) => sock.send(JSON.stringify(data)));
    }
    if(client.socket.readyState === 0) {
        setTimeout(() => client.send(data), 50);
    }
    else if(client.socket.readyState === 1){
        while(client.pending.length > 0 && client.socket.readyState === 1) client.pending.pop()(client.socket);
    }
    else{
        client.connect(() => client.send(data));
    }
};

client.connect = (cb) => {
    if(client.socket.readyState > 1){
        client.socket = WebSocketFactory();
        client.socket.onopen = () => {
            client.open = true;
            cb();
        };
        client.socket.onclose = () => {
            client.open = false;
        };
    }
};

client.consume = (topics, filters, messageHandler, errorHandler, closeHandler) => {
    client.socket.onmessage = (message) => websocketDataHandler(message, messageHandler, errorHandler);
    client.socket.onerror = errorHandler;
    client.socket.onclose = () => {
        client.open = false;
        closeHandler();
    };

    client.send({
        requestType: ".ConsumerRequest",
        topics: topics,
        limit: -1,
        limitAppliesFromStart: false,
        filters: filters || []
    });
};

client.disconnect = () => {

    client.send({
        requestType: ".CloseTaskRequest"
    });
    if(client.socket.readyState > 2){
        client.open = true;
        client.socket.close();
    }
};

export default client;