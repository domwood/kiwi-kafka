import React from 'react';
import KafkaTopics from './KafkaTopics';
import {render, unmountComponentAtNode} from "react-dom";
import {act} from "react-dom/test-utils";
import {AppDataContext} from "../../contexts/AppDataContext";

const topicList = [
    "exampleTestTopicOne", "exampleTestTopicTwo"
];

let container = null;
beforeEach(() => {
    container = document.createElement("div");
    document.body.appendChild(container);
});

afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
});

it('check kafka topics loaded on start', async () => {

    await act(async () => {
        render(
            <AppDataContext.Provider value={{topicList: topicList, topicListRefresh: () => {}}}>
                <KafkaTopics profiles={['write-admin', 'read-admin']}/>
            </AppDataContext.Provider>
            ,
            container);
    });

    let topicViewText = container.querySelector('#topicViewList').textContent;
    expect(topicViewText).toContain("exampleTestTopicOne");
    expect(topicViewText).toContain("exampleTestTopicTwo");
});