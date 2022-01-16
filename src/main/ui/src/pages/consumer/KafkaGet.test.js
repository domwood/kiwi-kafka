import React from 'react';
import * as ApiService from "../../services/ApiService";
import WebSocketService from "../../services/WebSocketService";
import {act} from "react-dom/test-utils";
import KafkaGet from "../consumer/KafkaGet";
import {render, unmountComponentAtNode} from "react-dom";
import {jest} from '@jest/globals';

jest.mock("../../services/ApiService");
jest.mock("../../services/WebSocketService");

const topicList = [
    "exampleTestTopicOne", "exampleTestTopicTwo"
];

let container = null;
beforeEach(() => {
    ApiService.getTopics.mockClear();
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
    ApiService.getTopics.mockImplementation((cb) => {
        cb(topicList);
    });

    await act(async () => {
        render(<KafkaGet isDownload={false} profiles={[]}/>, container);
    });

    let topicViewText = container.querySelector('#kafkaGetDataTitle').textContent;
    expect(topicViewText).toContain("Get Data From Kafka");
    expect(ApiService.getTopics).toHaveBeenCalledTimes(1);
});


