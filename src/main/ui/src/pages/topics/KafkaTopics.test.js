import React from 'react';
import ReactDOM from 'react-dom';
import KafkaTopics from './KafkaTopics';
import * as ApiService from "../../services/ApiService";

jest.mock("../../services/ApiService");

// const topicList = [
//     "exampleTestTopicOne", "exampleTestTopicTwo"
// ];

beforeEach(() => {
    ApiService.getTopics.mockClear();
});

it('renders without crashing', () => {
    const div = document.createElement('div');
    ReactDOM.render(<KafkaTopics profiles={['write-admin', 'read-admin']}/>, div);
    ReactDOM.unmountComponentAtNode(div);
});

// it('renders via enzyme', () => {
//     const wrapper = mount(<KafkaTopics profiles={['write-admin', 'read-admin']}/>);
//     const title = <h1>Kafka Topics</h1>
//     expect(wrapper.contains(title)).toEqual(true);
// });
//
//
// it('check kafka topics loaded on start', async () => {
//
//     ApiService.getTopics.mockImplementation((cb) => {
//         cb(topicList);
//     });
//
//     const wrapper = mount(<KafkaTopics profiles={['write-admin', 'read-admin']}/>);
//
//     await waitForState(wrapper, state => state.topicList && state.topicList.length > 0);
//
//     expect(wrapper.exists('#exampleTestTopicOne')).toBeTruthy();
//     expect(wrapper.exists('#exampleTestTopicTwo')).toBeTruthy();
//
//     expect(ApiService.getTopics).toHaveBeenCalledTimes(1);
//
// });