import React from 'react';
import KafkaTopics from './KafkaTopics';
import {render, unmountComponentAtNode} from "react-dom";
import {act} from "react-dom/test-utils";
import * as ApiService from "../../services/ApiService";

jest.mock("../../services/ApiService");

const topicList = [
    "exampleTestTopicOne", "exampleTestTopicTwo"
];

let container = null;
beforeEach(() => {
    ApiService.getTopics.mockClear();
    container = document.createElement("div");
    document.body.appendChild(container);
});

afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
});

it('check kafka topics loaded on start', async () => {

    ApiService.getTopics.mockImplementation((cb) => {
        cb(topicList);
    });

    await act(async () => {
        render(<KafkaTopics profiles={['write-admin', 'read-admin']}/>, container);
    });

    let topicViewText = container.querySelector('#topicViewList').textContent;
    expect(topicViewText).toContain("exampleTestTopicOne");
    expect(topicViewText).toContain("exampleTestTopicTwo");

    expect(ApiService.getTopics).toHaveBeenCalledTimes(1);
});