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
import KafkaPost from "./pages/KafkaPost";
import KafkaTopics from "./pages/KafkaTopics";

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
                        <Navbar color="light" light expand="md">
                            <NavbarBrand tag={Link} to="/" replace={true}>
                                <img src={logo} height="20" width="20" alt="Kiwi - Kafka Interactive Web Interface" />
                            </NavbarBrand>
                            <NavbarToggler onClick={this.toggle} />
                            <Collapse isOpen={this.state.isOpen} navbar>
                                <Nav className="ml-0" navbar>
                                    <NavItem>
                                        <NavLink tag={Link} to="/post" replace={true}>Kafka Post</NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink tag={Link} to="/topics" replace={true}>Kafka Topics</NavLink>
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
                            <Route path="/post" component={KafkaPost} />
                            <Route path="/topics" component={KafkaTopics} />
                            <Route redirectTo="/"/>
                        </Switch>
                    </div>
                </Router>
            </div>
        );
    }
}

export default App;