import logo from './imgs/Kiwi2.png';
import github from './imgs/github.svg';
import docker from './imgs/docker.svg';

import React, { Component } from 'react';
import {
    Collapse,
    Navbar,
    NavbarToggler,
    NavbarBrand,
    Nav,
    NavItem,
    NavLink
} from 'reactstrap';
import {HashRouter as Router, Link, Route, Switch} from "react-router-dom";
import KafkaHome from "./pages/brokers/KafkaHome";
import KafkaTopics from "./pages/topics/KafkaTopics";
import KafkaPost from "./pages/producer/KafkaPost";
import KafkaGet from "./pages/consumer/KafkaGet";
import KafkaConsumerGroups from "./pages/groups/KafkaConsumerGroups";
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';
import * as ApiService from "./services/ApiService";

class App extends Component {
    constructor(props) {
        super(props);

        this.toggle = this.toggle.bind(this);
        this.state = {
            isOpen: false,
            version: ''
        };

    }

    componentDidMount(){
        ApiService.getVersion((version) => {
            this.setState({
                version: version
            })
        }, () => toast.error("No connection to server"))
    }

    toggle() {
        this.setState({
            isOpen: !this.state.isOpen
        });
    }

    render() {

        return (
            <div>
                <Router>
                    <div>
                        <ToastContainer
                            position="top-right"
                            autoClose={3000}
                            hideProgressBar={false}
                            newestOnTop={false}
                            closeOnClick
                            rtl={false}
                            pauseOnVisibilityChange
                            draggable={false}
                            pauseOnHover
                        />
                        <Navbar color="light" light expand="md" className={"pt-0 pb-0"}>
                            <NavbarBrand>
                                <img src={logo} height="40" width="40" alt="Kiwi - Kafka Interactive Web Interface" />
                            </NavbarBrand>
                            <NavbarToggler onClick={this.toggle} />
                            <Collapse isOpen={this.state.isOpen} navbar>
                                <Nav className="ml-0" navbar>
                                    <NavItem>
                                        <NavLink tag={Link} to="/" replace={true} >Kafka Brokers</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/topics" replace={true} >Kafka Topics</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/groups" replace={true} >Kafka Consumers Groups</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/post" replace={true} >Kafka Post</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/get" replace={true} >Kafka Get</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/download" replace={true} >Kafka Download</NavLink>
                                    </NavItem>
                                </Nav>
                                <Nav className="ml-auto" navbar>
                                    <NavItem>
                                        <div style={{padding:0.5 +'rem'}}>
                                            Version: {this.state.version}
                                        </div>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink href="https://github.com/domwood/kiwi">
                                            <img src={github} height="20" width="20" alt="Github Link: https://github.com/domwood/kiwi" />
                                        </NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink href="https://hub.docker.com/r/dmwood/kiwi">
                                            <img src={docker} height="20" width="20" alt="Docker Link: https://hub.docker.com/r/dmwood/kiwi" />
                                        </NavLink>
                                    </NavItem>
                                </Nav>
                            </Collapse>
                        </Navbar>
                        <Switch>
                            <Route exact path="/" component={KafkaHome} />
                            <Route path="/topics" component={KafkaTopics} />
                            <Route path="/groups" component={KafkaConsumerGroups} />
                            <Route path="/post" component={KafkaPost} />
                            <Route path="/get" component={props => <KafkaGet {...props} isDownload={false} />} />
                            <Route path="/download" component={props => <KafkaGet {...props} isDownload={true} />} />
                            <Route redirectTo="/"/>
                        </Switch>
                    </div>
                </Router>
            </div>
        );
    }
}



export default App;