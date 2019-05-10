import WebSocketFactory from "./WebSocketFactory";

//TODO WIP POC

let socket = null;

const client = {};

client.pending = [];
client.open = false;

client.connect = () => {
    if(socket === null || socket.readyState > 1){
        socket = WebSocketFactory();
        socket.onopen = () => {
            while(client.pending.length > 0) client.pending.pop()();
            client.open = true;
        }
    }
};

client.consume = (topics, filters, messageHandler, errorHandler, onClose) => {
    socket.onmessage = messageHandler;
    socket.onerror = errorHandler;
    socket.onclose = onClose;

    let fn = () => {
        socket.send(JSON.stringify({
            requestType: ".ConsumerRequest",
            topics: topics,
            limit: -1,
            limitAppliesFromStart: false,
            filters: filters || []
        }))
    };

    client.pending.push(fn);

    if(client.open){
        while(client.pending.length > 0) client.pending.pop()();
    }
};

export default client;