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
import {AppDataContext, CLOSED_STATE} from "../../../contexts/AppDataContext";

const TIMESTAMP = "TIMESTAMP";
const STARTS_WITH = "STARTS_WITH";
const KEY = "KEY";
const VALUE = "VALUE";
const HEADER_KEY = "HEADER_KEY";
const HEADER_VALUE = "HEADER_VALUE";
const PARTITION = "PARTITION";
const OFFSET = "OFFSET";
const MATCHES = "MATCHES";
const NOT_MATCHES = "NOT_MATCHES";
const ENDS_WITH = "ENDS_WITH";
const CONTAINS = "CONTAINS";
const NOT_CONTAINS = "NOT_CONTAINS";
const REGEX = "REGEX";
const LESS_THAN = "LESS_THAN";
const GREATER_THAN = "GREATER_THAN";

class FilterConfigurer extends Component {

    static contextType = AppDataContext

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
                    filter: filter.trimWhitespace ? ((filter.filter || '') + '').trim() : filter.filter,
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
        filters[index].filter = filter;
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

    isNumericFilter = (filterApplication) => {
        return filterApplication === PARTITION ||
            filterApplication === OFFSET ||
            filterApplication === TIMESTAMP
    }

    isNumericFilterAtIndex = (index) => {
        let filter = this.state.filters[index];
        return this.isNumericFilter(filter.filterApplication);
    }

    setFilterApplication = (filterApplication, index) => {
        let filters = this.state.filters;
        if (this.isNumericFilter(filterApplication) !== this.isNumericFilterAtIndex(index)) {
            filters[index].filterType = GREATER_THAN;
        }
        if (filterApplication === TIMESTAMP) {
            filters[index].filter = new Date().getTime();
        }
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
                filterType: STARTS_WITH,
                filterApplication: KEY,
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

    asDateString = (timestamp) => {
        timestamp = timestamp ? parseInt(timestamp) : new Date().getTime();
        timestamp = timestamp < 100000000000000 && timestamp >= 0 ? timestamp : new Date().getTime();
        return new Date(timestamp).toISOString().replace(/Z$/g, "");
    }

    updateTimestamp = (event, index) => {
        let filters = this.state.filters;
        filters[index] = (event.target.valueAsDate || new Date()).getTime();
        this.setState({
            filters: filters
        }, this.updateParent)
    }

    render() {
        let disabled = this.context.consumingState !== CLOSED_STATE;
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
                                                className={"filterButton"}
                                                isOpen={this.state.filters[index].filterApplicationButtonOpen}
                                                toggle={() => this.toggleFilterTypeApplicationButton(index)}
                                                disabled={disabled}
                                            >
                                                <DropdownToggle caret>
                                                    {this.state.filters[index].filterApplication}
                                                </DropdownToggle>
                                                <DropdownMenu>
                                                    <DropdownItem header>Filter Applies To</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication(KEY, index)}>Key</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication(VALUE, index)}>Value</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication(HEADER_KEY, index)}>Header
                                                        Key</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication(HEADER_VALUE, index)}>Header
                                                        Value</DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication(PARTITION, index)}>Partition
                                                    </DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication(OFFSET, index)}>Offset
                                                    </DropdownItem>
                                                    <DropdownItem
                                                        onClick={() => this.setFilterApplication(TIMESTAMP, index)}>Timestamp
                                                    </DropdownItem>
                                                </DropdownMenu>
                                            </ButtonDropdown>
                                            <ButtonDropdown
                                                className={"filterButton"}
                                                isOpen={this.state.filters[index].filterTypeButtonOpen}
                                                toggle={() => this.toggleFilterTypeButton(index)}
                                                disabled={disabled}
                                            >
                                                <DropdownToggle caret>
                                                    {this.state.filters[index].filterType}
                                                </DropdownToggle>
                                                {
                                                    !this.isNumericFilterAtIndex(index) ?
                                                        <DropdownMenu>
                                                            <DropdownItem header>Filter Type</DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(MATCHES, index)}>
                                                                Matches
                                                            </DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(NOT_MATCHES, index)}>
                                                                Not Matches
                                                            </DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(STARTS_WITH, index)}>Starts
                                                                With</DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(ENDS_WITH, index)}>Ends
                                                                With</DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(CONTAINS, index)}>Contains</DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(NOT_CONTAINS, index)}>Not
                                                                contains</DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(REGEX, index)}>Regex</DropdownItem>
                                                        </DropdownMenu>
                                                        :
                                                        <DropdownMenu>
                                                            <DropdownItem header>Filter Type</DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(GREATER_THAN, index)}>
                                                                Greater Than
                                                            </DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(LESS_THAN, index)}>
                                                                Less Than
                                                            </DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(MATCHES, index)}>
                                                                Matches
                                                            </DropdownItem>
                                                            <DropdownItem
                                                                onClick={() => this.setFilterType(NOT_MATCHES, index)}>
                                                                Not Matches
                                                            </DropdownItem>
                                                        </DropdownMenu>
                                                }

                                            </ButtonDropdown>
                                            {
                                                this.state.filters[index].filterApplication === TIMESTAMP ?
                                                    <InputGroupText id={"datetime" + index}>
                                                        <Input addon
                                                               type="datetime-local"
                                                               aria-label="Time"
                                                               step={".001"}
                                                               value={this.asDateString(this.state.filters[index].filter)}
                                                               style={{borderRadius: "unset", width: "350px"}}
                                                               onChange={(event) =>
                                                                   this.setFilter(new Date(event.target.value).getTime(), index)}
                                                               disabled={disabled}
                                                        />
                                                    </InputGroupText> : <React.Fragment/>
                                            }
                                            <Input
                                                type="text"
                                                name="filter"
                                                id="filter"
                                                value={this.state.filters[index].filter}
                                                onChange={event => this.setFilter(event.target.value, index)}
                                                disabled={disabled}
                                            />
                                            <InputGroupText>
                                                {
                                                    this.state.filters[index].filterType !== REGEX && !this.isNumericFilterAtIndex(index) ?
                                                        <div>
                                                            <Button onClick={() => this.setCaseSensitive(index)}
                                                                    color={this.state.filters[index].isCaseSensitive ? 'warning' : 'success'}
                                                                    disabled={disabled}
                                                            >
                                                                {this.state.filters[index].isCaseSensitive ? 'Case Sensitive' : 'Case Insensitive'}
                                                            </Button>
                                                        </div>
                                                        : <React.Fragment/>
                                                }
                                            </InputGroupText>
                                            {
                                                this.state.filters[index].filterType !== REGEX && !this.isNumericFilterAtIndex(index) ?
                                                    <InputGroupText id={"auto" + index}>
                                                        <div className={"input-group-text-padded"}>Auto-Trim: &nbsp;
                                                            <Input addon
                                                                   type="checkbox"
                                                                   aria-label="Check to trim whitespace"
                                                                   checked={this.state.filters[index].trimWhitespace}
                                                                   onChange={() => this.setWhiteSpaceTrim(index)}
                                                                   disabled={disabled}
                                                            />
                                                            <Tooltip target={"auto" + index}
                                                                     placement={"top"}
                                                                     toggle={() => this.setWhiteSpaceTrimToolTipToggle(index)}
                                                                     isOpen={this.state.filters[index].whitespaceToggle}
                                                                     disabled={disabled}
                                                            >
                                                                Automatically remove whitespace from start/end of filter
                                                                string
                                                            </Tooltip>
                                                        </div>
                                                    </InputGroupText> : <React.Fragment/>
                                            }


                                        </InputGroup>
                                        {
                                            index === this.state.filters.length - 1 ?
                                                <div className={"Gap"}>
                                                    <ButtonGroup>
                                                        <Button
                                                            onClick={() => this.addFilter()}
                                                            color={'success'}
                                                            disabled={disabled}
                                                        >
                                                            + Add
                                                        </Button>
                                                        <Button
                                                            onClick={() => this.removeFilter()}
                                                            color={'warning'}
                                                            disabled={disabled}
                                                        >
                                                            - Remove
                                                        </Button>
                                                    </ButtonGroup>
                                                </div>
                                                : <React.Fragment/>
                                        }

                                    </ListGroupItem>
                                )
                            })
                        }

                    </ListGroup>
                    :
                    <Button color="secondary"
                            size="sm" block
                            onClick={this.addFilter}
                            width={'100%'}
                            disabled={disabled}
                    >Include Message Filter</Button>
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