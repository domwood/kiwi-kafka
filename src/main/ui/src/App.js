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
                <Navbar color="light" light expand="md">
                    <NavbarBrand href="/"><img src={logo} height="20" width="20" alt="Kiwi - Kafka Interactive Web Interface"></img></NavbarBrand>
                    <NavbarToggler onClick={this.toggle} />
                    <Collapse isOpen={this.state.isOpen} navbar>
                        <Nav className="ml-0" navbar>
                            <NavItem>
                                <NavLink href="/post">Kafka Post</NavLink>
                            </NavItem>
                            <NavItem>
                                <NavLink href="/topics">Kafka Topics</NavLink>
                            </NavItem>
                        </Nav>
                        <Nav className="ml-auto" navbar>
                            <NavItem>
                                <NavLink href="https://github.com/domwood"><img src={github} height="20" width="20" alt="Github Link: https://github.com/domwood" />
                                </NavLink>
                            </NavItem>
                        </Nav>
                    </Collapse>
                </Navbar>
            </div>
        );
    }
}

export default App;