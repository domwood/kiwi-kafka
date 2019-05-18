import WebSocketFactory from "./WebSocketFactory";

//TODO WIP POC
const client = {
    socket:{
        readyState: 4, //0 Connected, 1 Connecting, 2 Closing, 3 Closed (∴ 4 =~ UNCREATED)
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
    if(client.open && client.socket.readyState < 2) {
        while(client.pending.length > 0) client.pending.pop()(client.socket);
    }
};

client.connect = () => {
    if(client.socket.readyState > 1){
        client.socket = WebSocketFactory();
        client.socket.onopen = () => {
            setTimeout(() => {
                client.open = true;
                client.send();
            }, 10)
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