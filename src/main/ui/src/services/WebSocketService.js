import WebSocketFactory from "./WebSocketFactory";

//TODO WIP POC

let socket = {
    readyState: 4, //0 Connected, 1 Connecting, 2 Closing, 3 Closed (∴ 4 =~ UNCREATED)
    send: () => console.warn("Socket send called before being socket created"),
    close: () => console.warn("Socket close called before being socket created")
};

const client = {};

client.pending = [];
client.open = false;

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
        client.pending.push(() => socket.send(JSON.stringify(data)));
    }
    if(client.open) {
        while(client.pending.length > 0) client.pending.pop()();
    }
};

client.connect = () => {
    if(socket.readyState > 1){
        socket = WebSocketFactory();
        socket.onopen = () => {
            setTimeout(() => {
                client.open = true;
                client.send();
            }, 10)
        }
    }
};

client.consume = (topics, filters, messageHandler, errorHandler, closeHandler) => {
    socket.onmessage = (message) => websocketDataHandler(message, messageHandler, errorHandler);
    socket.onerror = errorHandler;
    socket.onclose = closeHandler;

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
    if(socket.readyState > 2){
        client.open = true;
        socket.close();
    }
};

export default client;