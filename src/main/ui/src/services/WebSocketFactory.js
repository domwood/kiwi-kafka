import api from "./ApiConfig";

export default () => new WebSocket(api.webSocket);