import logo from './imgs/Kiwi2.png';
import github from './imgs/github.svg';

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
import KafkaHome from "./pages/KafkaHome";
import KafkaTopics from "./pages/KafkaTopics";
import KafkaPost from "./pages/KafkaPost";
import KafkaGet from "./pages/KafkaGet";
import {ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';

class App extends Component {
    constructor(props) {
        super(props);

        this.toggle = this.toggle.bind(this);
        this.state = {
            isOpen: false
        };

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
                            <NavbarBrand tag={Link} to="/" replace={true}>
                                <img src={logo} height="40" width="40" alt="Kiwi - Kafka Interactive Web Interface" />
                            </NavbarBrand>
                            <NavbarToggler onClick={this.toggle} />
                            <Collapse isOpen={this.state.isOpen} navbar>
                                <Nav className="ml-0" navbar>
                                    <NavItem>
                                        <NavLink tag={Link} to="/topics" replace={true}>Kafka Topics</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/post" replace={true}>Kafka Post</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/get" replace={true}>Kafka Get</NavLink>
                                    </NavItem>
                                </Nav>
                                <Nav className="ml-auto" navbar>
                                    <NavItem>
                                        <NavLink href="https://github.com/domwood">
                                            <img src={github} height="20" width="20" alt="Github Link: https://github.com/domwood" />
                                        </NavLink>
                                    </NavItem>
                                </Nav>
                            </Collapse>
                        </Navbar>
                        <Switch>
                            <Route exact path="/" component={KafkaHome} />
                            <Route path="/topics" component={KafkaTopics} />
                            <Route path="/post" component={KafkaPost} />
                            <Route path="/get" component={KafkaGet} />
                            <Route redirectTo="/"/>
                        </Switch>
                    </div>
                </Router>
            </div>
        );
    }
}



export default App;