import React from 'react';
import ReactDOM from 'react-dom';
import KafkaGet from './KafkaGet';
import * as ApiService from "../../services/ApiService";
import WebSocketService from "../../services/WebSocketService";
import {mount} from "enzyme/build";
import { waitForState } from 'enzyme-async-helpers';

jest.mock("../../services/ApiService");
jest.mock("../../services/WebSocketService");

//Apparently need this junk??
jest.mock('popper.js', () => {
    const PopperJS = jest.requireActual('popper.js');

    return class {
        static placements = PopperJS.placements;

        constructor() {
            return {
                destroy: () => {},
                scheduleUpdate: () => {}
            };
        }
    };
});

const testDataResponse = {
    responseType:".ImmutableConsumerResponse",
    messages:[
        {
            responseType:".ImmutableConsumedMessage",
            timestamp:1557561292030,
            partition:7,
            offset:30,
            message:"testData1",
            key:"testKey1",
            headers:{}
        },
        {
            responseType:".ImmutableConsumedMessage",
            timestamp:1557561192030,
            partition:8,
            offset:45,
            message:"testData2",
            key:"testKey2",
            headers:{}
        }
    ]
};


const topicList = [
    "exampleTestTopicOne", "exampleTestTopicTwo"
];

beforeEach(() => {
    ApiService.getTopics.mockClear();
    ApiService.consume.mockClear();
    WebSocketService.consume.mockClear();
});

it('renders without crashing', () => {
    const div = document.createElement('div');
    ReactDOM.render(<KafkaGet isDownload={false} profiles={[]}/>, div);
    ReactDOM.unmountComponentAtNode(div);
});

it('renders via enzyme', () => {
    const wrapper = mount(<KafkaGet isDownload={false} profiles={[]}/>);
    const title = <h1>Get Data From Kafka</h1>
    expect(wrapper.contains(title)).toEqual(true);
});


