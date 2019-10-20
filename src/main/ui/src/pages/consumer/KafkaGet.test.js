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


it('check kafka messages from websocket', async () => {

    WebSocketService.connect.mockImplementation(cb => cb(), () => {});

    WebSocketService.consume.mockImplementation((topics, filters, startPosition, cb, eb, close) => {
        cb(testDataResponse);
    });

    ApiService.getTopics.mockImplementation((cb, eb) => {
        cb(topicList);
    });

    const wrapper = mount(<KafkaGet isDownload={false} profiles={[]}/>);
    
    wrapper.find('.rbt-input-main').at(0)
         .simulate('change', { target: { value: 'testDataTopic' } })

    wrapper.find("#messageLimitInput").at(0)
        .simulate('change', { target: { value: 2 } });

    wrapper.find('#consumeViaWebSocketButton').at(0)
        .simulate('click');

    await waitForState(wrapper, state => state.messages && state.messages.length > 0);

    expect(wrapper.exists(`#record_row_${testDataResponse.messages[0].partition}_${testDataResponse.messages[0].offset}`)).toBeTruthy();
    expect(wrapper.exists(`#record_row_${testDataResponse.messages[1].partition}_${testDataResponse.messages[1].offset}`)).toBeTruthy();

    expect(ApiService.getTopics).toHaveBeenCalledTimes(1);
    expect(WebSocketService.consume).toHaveBeenCalledTimes(1);

    expect(WebSocketService.consume).toHaveBeenCalledWith(
        ['testDataTopic'],
        [],
        0.0,
        expect.any(Function),
        expect.any(Function),
        expect.any(Function)
    );
});

