import React, { Component } from "react";
import {
    Button,
    DropdownItem,
    DropdownMenu,
    DropdownToggle, Input,
    InputGroup,
    InputGroupButtonDropdown, Jumbotron
} from "reactstrap";
import PropTypes from "prop-types";
import "./../App.css";

class FilterConfigurer extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id : props.id,
            name : props.name,
            useFilter: false,
            filterTypeButtonOpen: false,
            filterApplicationButtonOpen: false,
            filter: '',
            filterType: 'STARTS_WITH',
            filterApplication: 'KEY',
            isCaseSensitive: false
        };
    }

    updateParent = () => {
        if(this.validateFilter()){
            let filterObject = {
                filter: this.state.filter,
                filterType: this.state.filterType,
                filterApplication: this.state.filterApplication,
                isCaseSensitive: this.state.isCaseSensitive,
                headerKey: undefined
            };
            this.props.onUpdate(filterObject);
        }
        else this.props.onUpdate(null);
    };

    validateFilter = () =>
        this.state.useFilter &&
        this.state.filter &&
        this.state.filterType &&
        this.state.filterApplication;

    toggleFilter = () => {
        this.setState({
            useFilter : !this.state.useFilter
        }, this.updateParent)
    };

    toggleFilterTypeButton = () => {
        this.setState({
            filterTypeButtonOpen: !this.state.filterTypeButtonOpen
        }, this.updateParent);
    };

    toggleFilterTypeApplicationButton = () => {
        this.setState({
            filterApplicationButtonOpen: !this.state.filterApplicationButtonOpen
        }, this.updateParent);
    };

    setFilter = (filter) => {
        this.setState({
            filter:filter
        }, this.updateParent);
    };

    setFilterType = (filterType) => {
        this.setState({
            filterType: filterType
        }, this.updateParent)
    };

    setFilterApplication = (filterApplication) => {
        this.setState({
            filterApplication: filterApplication
        }, this.updateParent)
    };

    setCaseSensitive = () => {
        this.setState({
            isCaseSensitive: !this.state.isCaseSensitive
        }, this.updateParent)
    };


    render() {
        return (
            <div id={this.state.id}>

                <div className="mt-lg-1"/>
                <div className="Gap"/>
                {   this.state.useFilter ?

                    <Jumbotron>
                        <InputGroup>
                            <InputGroupButtonDropdown addonType="prepend" isOpen={this.state.filterApplicationButtonOpen} toggle={this.toggleFilterTypeApplicationButton}>
                            <DropdownToggle split outline />
                                <DropdownMenu>
                                <DropdownItem header>Filter Applies To</DropdownItem>
                                <DropdownItem onClick={() => this.setFilterApplication("KEY")}>Key</DropdownItem>
                                <DropdownItem onClick={() => this.setFilterApplication("MESSAGE")}>Message</DropdownItem>
                                <DropdownItem onClick={() => this.setFilterApplication("HEADER_KEY")}>Header Key</DropdownItem>
                                <DropdownItem onClick={() => this.setFilterApplication("HEADER_VALUE")}>Header Value</DropdownItem>
                            </DropdownMenu>
                            <Button disabled>{this.state.filterApplication}</Button>
                            </InputGroupButtonDropdown>
                            <InputGroupButtonDropdown addonType="prepend" isOpen={this.state.filterTypeButtonOpen} toggle={this.toggleFilterTypeButton}>
                                <DropdownToggle split outline />
                                <DropdownMenu>
                                    <DropdownItem header>Filter Type</DropdownItem>
                                    <DropdownItem onClick={() => this.setFilterType("MATCHES")}>Matches</DropdownItem>
                                    <DropdownItem onClick={() => this.setFilterType("STARTS_WITH")}>Starts With</DropdownItem>
                                    <DropdownItem onClick={() => this.setFilterType("ENDS_WITH")}>Ends With</DropdownItem>
                                    <DropdownItem onClick={() => this.setFilterType("CONTAINS")}>Contains</DropdownItem>
                                    <DropdownItem onClick={() => this.setFilterType("REGEX")}>Regex</DropdownItem>
                                </DropdownMenu>
                                <Button disabled>{this.state.filterType}</Button>
                            </InputGroupButtonDropdown>
                            <Input
                                type="text"
                                name="filter"
                                id="filter"
                                defaultValue={this.state.filter}
                                onChange={event => this.setFilter(event.target.value)}
                            />
                            {
                                this.state.filterType !== 'REGEX' ?
                                    <Button onClick={() => this.setCaseSensitive()}>
                                        {this.state.isCaseSensitive ? 'Case Insensitive' : 'Case Sensitive'}
                                    </Button>
                                    : ''
                            }
                        </InputGroup>
                    </Jumbotron>
                    : ''
                }

                <Button size="sm" block onClick={this.toggleFilter} width={'100%'}>{this.state.useFilter ? 'Remove Filter' : 'Include Message Filter'}</Button>

                <div className="mt-lg-1"/>
            </div>
        )
    }
}

FilterConfigurer.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    onUpdate: PropTypes.func.isRequired
};

export default FilterConfigurer;