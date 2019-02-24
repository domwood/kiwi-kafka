import React from 'react';
import {BrowserRouter as Router, Route} from "react-router-dom";
import App from "./App";
import KafkaPost from "./pages/KafkaPost";
import KafkaTopics from "./pages/KafkaTopics";

const routing = (
    <Router>
        <div>
            <Route path="#/" component={App} />
            <Route path="#/post" component={KafkaPost} />
            <Route path="#/topics" component={KafkaTopics} />
        </div>
    </Router>
)

export default routing;