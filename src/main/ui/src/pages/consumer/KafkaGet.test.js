import React from 'react';
import * as ApiService from "../../services/ApiService";
import WebSocketService from "../../services/WebSocketService";
import {act} from "react-dom/test-utils";
import KafkaGet from "../consumer/KafkaGet";
import {render, unmountComponentAtNode} from "react-dom";
import {jest} from '@jest/globals';
import {AppDataContext, CLOSED_STATE} from "../../contexts/AppDataContext";

jest.mock("../../services/ApiService");
jest.mock("../../services/WebSocketService");

const topicList = [
    "exampleTestTopicOne", "exampleTestTopicTwo"
];

let container = null;
beforeEach(() => {
    ApiService.consume.mockClear();
    WebSocketService.consume.mockClear();

    container = document.createElement("div");
    document.body.appendChild(container);
});

afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
});

it('check renders Kafka get page', async () => {

    await act(async () => {
        render(
            <AppDataContext.Provider value={{
                topicList: topicList,
                topicData: {},
                setConsumingState: () => {},
                consumingState: CLOSED_STATE
            }}>
                <KafkaGet isDownload={false} profiles={[]}/>
            </AppDataContext.Provider>,
            container);
    });

    let topicViewText = container.querySelector('#kafkaGetDataTitle').textContent;
    expect(topicViewText).toContain("Get Data From Kafka");

});


