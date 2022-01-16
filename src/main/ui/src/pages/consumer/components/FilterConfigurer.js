import React, {Component} from "react";
import {
    Button,
    ButtonDropdown,
    ButtonGroup,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Input,
    InputGroup,
    InputGroupText,
    ListGroup,
    ListGroupItem,
    Tooltip
} from "reactstrap";
import PropTypes from "prop-types";
import "../../../App.css";

class FilterConfigurer extends Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            name: props.name,
            useFilter: false,
            filters: []
        };
    }

    updateParent = () => {
        if (this.state.useFilter && this.state.filters.every(this.validateFilter)) {
            this.props.onUpdate(this.state.filters.map(filter => {
                return {
                    filter: filter.filter,
                    filterType: filter.filterType,
                    filterApplication: filter.filterApplication,
                    isCaseSensitive: filter.isCaseSensitive
                }
            }));
        } else this.props.onUpdate([]);
    };

    validateFilter = (filter) =>
        filter.filter &&
        filter.filterType &&
        filter.filterApplication;

    toggleFilterTypeButton = (index) => {
        let filters = this.state.filters;
        filters[index].filterTypeButtonOpen = !filters[index].filterTypeButtonOpen;
        this.setState({
            filters: filters
        }, this.updateParent);
    };

    toggleFilterTypeApplicationButton = (index) => {
        let filters = this.state.filters;
        filters[index].filterApplicationButtonOpen = !filters[index].filterApplicationButtonOpen;
        this.setState({
            filterApplicationButtonOpen: !this.state.filterApplicationButtonOpen
        }, this.updateParent);
    };

    setFilter = (filter, index) => {
        let filters = this.state.filters;
        filters[index].filter = filters[index].trimWhitespace ? (filter || '').trim() : filter;
        this.setState({
            filters: filters
        }, this.updateParent);
    };

    setFilterType = (filterType, index) => {
        let filters = this.state.filters;
        filters[index].filterType = filterType;
        this.setState({
            filters: filters
        }, this.updateParent)
    };

    setFilterApplication = (filterApplication, index) => {
        let filters = this.state.filters;
        filters[index].filterApplication = filterApplication;

        this.setState({
            filters: filters
        }, this.updateParent)
    };

    setCaseSensitive = (index) => {
        let filters = this.state.filters;
        filters[index].isCaseSensitive = !filters[index].isCaseSensitive;
        this.setState({
            filters: filters
        }, this.updateParent)
    };

    setWhiteSpaceTrim = (index) => {
        let filters = this.state.filters;
        filters[index].trimWhitespace = !filters[index].trimWhitespace;
        this.setState({
            filters: filters
        }, () => {
            this.setFilter(filters[index].filter, index)
        })
    };

    setWhiteSpaceTrimToolTipToggle = (index) => {
        let filters = this.state.filters;
        filters[index].whitespaceToggle = !filters[index].whitespaceToggle;
        this.setState({
            filters: filters
        });
    };

    addFilter = () => {
        this.setState({
            useFilter: true,
            filters: [...this.state.filters, {
                filterTypeButtonOpen: false,
                filterApplicationButtonOpen: false,
                filter: '',
                filterType: 'STARTS_WITH',
                filterApplication: 'KEY',
                isCaseSensitive: false,
                trimWhitespace: true,
                whitespaceToggle: false
            }]
        }, this.updateParent);
    };

    removeFilter = () => {
        let useFilter = this.state.filters.length > 1;
        this.setState({
            useFilter: useFilter,
            filters: useFilter ? this.state.filters.slice(0, -1) : []
        }, this.updateParent);
    };

    render() {
        return (
            <div id={this.state.id}>

                <div className="mt-lg-1"/>
                <div className="Gap"/>
                {this.state.useFilter ?

                    <ListGroup>
                        {
                            this.state.filters.map((filter, index) => {
                                return (
                                    <ListGroupItem key={index} className={"ListGroupNoHorizontalPad"}>
                                        <InputGroup>
                                            <ButtonDropdown
                                                isOpen={this.state.filters[index].filterApplicationButtonOpen}
                                                toggle={() => this.toggleFilterTypeApplicationButton(index)}>
                                                <DropdownToggle caret>
                                                    {this.state.filters[index].filterApplication}
                                                </DropdownToggle>
                                                <DropdownMenu>
                                                    <DropdownItem header>Filter Applies To</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication("KEY", index)}>Key</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication("VALUE", index)}>Value</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication("HEADER_KEY", index)}>Header
                                                        Key</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication("HEADER_VALUE", index)}>Header
                                                        Value</DropdownItem>
                                                </DropdownMenu>
                                            </ButtonDropdown>
                                            <ButtonDropdown isOpen={this.state.filters[index].filterTypeButtonOpen}
                                                            toggle={() => this.toggleFilterTypeButton(index)}>
                                                <DropdownToggle caret>
                                                    {this.state.filters[index].filterType}
                                                </DropdownToggle>
                                                <DropdownMenu>
                                                    <DropdownItem header>Filter Type</DropdownItem>
                                                    <DropdownItem onClick={() => this.setFilterType("MATCHES", index)}>
                                                        Matches
                                                    </DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterType("STARTS_WITH", index)}>Starts
                                                        With</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterType("ENDS_WITH", index)}>Ends
                                                        With</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterType("CONTAINS", index)}>Contains</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterType("NOT_CONTAINS", index)}>Not
                                                        contains</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterType("REGEX", index)}>Regex</DropdownItem>
                                                </DropdownMenu>
                                            </ButtonDropdown>
                                            <Input
                                                type="text"
                                                name="filter"
                                                id="filter"
                                                value={this.state.filters[index].filter}
                                                onChange={event => this.setFilter(event.target.value, index)}
                                            />
                                            <InputGroupText addonType="append">
                                                {
                                                    this.state.filters[index].filterType !== 'REGEX' ?
                                                        <div>
                                                            <Button onClick={() => this.setCaseSensitive(index)}
                                                                    color={this.state.filters[index].isCaseSensitive ? 'warning' : 'success'}>
                                                                {this.state.filters[index].isCaseSensitive ? 'Case Sensitive' : 'Case Insensitive'}
                                                            </Button>
                                                        </div>
                                                        : null
                                                }
                                            </InputGroupText>
                                            <InputGroupText id={"auto" + index}>
                                                <div className={"input-group-text-padded"}>Auto-Trim: &nbsp;
                                                    <Input addon
                                                           type="checkbox"
                                                           aria-label="Check to trim whitespace"
                                                           checked={this.state.filters[index].trimWhitespace}
                                                           onChange={() => this.setWhiteSpaceTrim(index)}/>
                                                    <Tooltip target={"auto" + index}
                                                             placement={"top"}
                                                             toggle={() => this.setWhiteSpaceTrimToolTipToggle(index)}
                                                             isOpen={this.state.filters[index].whitespaceToggle}>
                                                        Automatically remove whitespace from start/end of filter string
                                                    </Tooltip>
                                                </div>
                                            </InputGroupText>
                                        </InputGroup>
                                        {
                                            index === this.state.filters.length - 1 ?
                                                <div className={"Gap"}>
                                                    <ButtonGroup>
                                                        <Button onClick={() => this.addFilter()} color={'success'}>
                                                            + Add
                                                        </Button>
                                                        <Button onClick={() => this.removeFilter()} color={'warning'}>
                                                            - Remove
                                                        </Button>
                                                    </ButtonGroup>
                                                </div>
                                                : null
                                        }

                                    </ListGroupItem>
                                )
                            })
                        }

                    </ListGroup>
                    :
                    <Button color="secondary" size="sm" block onClick={this.addFilter} width={'100%'}>Include Message
                        Filter</Button>
                }

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