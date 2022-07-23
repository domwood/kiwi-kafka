import logo from './imgs/Kiwi2.png';
import github from './imgs/github.svg';
import docker from './imgs/docker.svg';

import React, {Component} from 'react';
import {Nav, Navbar, NavbarBrand, NavItem, NavLink} from 'reactstrap';
import {HashRouter as Router, Link, Route, Routes} from "react-router-dom";
import KafkaHome from "./pages/brokers/KafkaHome";
import KafkaTopics from "./pages/topics/KafkaTopics";
import KafkaPost from "./pages/producer/KafkaPost";
import KafkaGet from "./pages/consumer/KafkaGet";
import KafkaConsumerGroups from "./pages/groups/KafkaConsumerGroups";
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';
import * as ApiService from "./services/ApiService";
import ClusterChooser from "./pages/common/ClusterChooser";
import AppData from "./contexts/AppData";

class App extends Component {
    constructor(props) {
        super(props);

        this.state = {
            isOpen: false,
            version: '',
            profiles: [],
            clusterDropDownOpen: false,
            workerInfo: ""
        };
    }

    componentDidMount() {
        this.mounted = true;
        ApiService.getVersion((version) => {
            if (this.mounted) {
                this.setState({
                    version: version
                })
            }
        }, () => toast.error("No connection to server"));

        ApiService.getProfiles((profiles) => {
            if (this.mounted) {
                this.setState({
                    profiles: profiles
                })
            }
        }, () => toast.error("No connection to server"));

    }

    componentWillUnmount() {
        this.mounted = false;
    }

    render() {

        return (
            <div>
                <AppData>
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
                                theme={"colored"}
                                pauseOnHover
                            />
                            <Navbar color="light" light expand="md" className={"pt-0 pb-0"}>
                                <Nav className="ml-auto" navbar>
                                    <NavbarBrand>
                                        <img style={{"marginTop": "-0.375rem"}} src={logo} height="30" width="30"
                                             alt="Kiwi - Kafka Interactive Web Interface"/>
                                    </NavbarBrand>
                                    <NavItem>
                                        <NavLink tag={Link} to="/" replace={true}>Kafka Brokers</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/topics" replace={true}>Kafka Topics</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/groups" replace={true}>Kafka Consumers Groups</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/post" replace={true}>Kafka Post</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/get" replace={true}>Kafka Get</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/download" replace={true}>Kafka Download</NavLink>
                                    </NavItem>
                                </Nav>
                                <Nav className="mr-auto" navbar>
                                    <div style={{padding: '3px'}}>
                                        <ClusterChooser/>
                                    </div>
                                    <NavItem>
                                        <div style={{padding: '11px'}}>
                                            Version: {this.state.version}
                                        </div>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink href="https://github.com/domwood/kiwi">
                                            <img src={github} height="20" width="20"
                                                 alt="Github Link: https://github.com/domwood/kiwi"/>
                                        </NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink href="https://hub.docker.com/r/dmwood/kiwi">
                                            <img src={docker} height="20" width="20"
                                                 alt="Docker Link: https://hub.docker.com/r/dmwood/kiwi"/>
                                        </NavLink>
                                    </NavItem>
                                </Nav>
                            </Navbar>

                            <Routes>
                                <Route exact path="/" element={<KafkaHome profiles={this.state.profiles}/>}/>
                                <Route path="/topics" element={<KafkaTopics profiles={this.state.profiles}/>}/>
                                <Route path="/groups" element={<KafkaConsumerGroups profiles={this.state.profiles}/>}/>
                                <Route path="/post" element={<KafkaPost profiles={this.state.profiles}/>}/>
                                <Route path="/get"
                                       element={<KafkaGet isDownload={false} profiles={this.state.profiles}/>}/>
                                <Route path="/download"
                                       element={<KafkaGet isDownload={true} profiles={this.state.profiles}/>}/>
                                <Route redirectTo="/"/>
                            </Routes>
                        </div>
                    </Router>
                </AppData>
            </div>
        );
    }
}


export default App;