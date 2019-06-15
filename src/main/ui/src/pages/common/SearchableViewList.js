import React, {Component} from "react";
import {InputGroup, InputGroupAddon, InputGroupText, ListGroup, ListGroupItem} from "reactstrap";
import "../../App.css";
import PropTypes from "prop-types";
import {Typeahead} from "react-bootstrap-typeahead";

class SearchableViewList extends Component {

    constructor(props) {
        super(props);

        this.state = {
            unfilteredList: props.elementList,
            filteredList: props.elementList
        };
    }

    componentDidUpdate(prevProps) {
        if(prevProps.elementList !== this.props.elementList){
            this.setState({
                unfilteredList: this.props.elementList,
                filteredList: this.props.elementList
            }, () => this.filterList(this.state.filterWord));
        }
    }

    filterList = (filterWord) => {
        if (filterWord && filterWord.length > 0) {
            this.setState({
                filterWord: filterWord,
                filteredList: this.state.unfilteredList
                    .filter(element => element.toLowerCase().search(filterWord.toLowerCase()) !== -1)
            })
        } else {
            this.setState({
                filterWord: '',
                filteredList: this.state.unfilteredList
            })
        }
    };


    render() {
        return (
            <ListGroup>
                <ListGroupItem>
                    <InputGroup>
                        <InputGroupAddon addonType="prepend">
                            <InputGroupText>Filter:</InputGroupText>
                        </InputGroupAddon>
                        <Typeahead
                            id={"searchList"}
                            onChange={selected => selected && selected[0] ? this.filterList(selected[0]) : ''}
                            onInputChange={i => this.filterList(i || '')}
                            options={this.state.unfilteredList}
                            className={"StretchedInput"}
                        />
                    </InputGroup>
                </ListGroupItem>
                {
                    this.state.filteredList.map(this.props.elementViewProvider)
                }
            </ListGroup>
        );
    }
}

SearchableViewList.propTypes = {
    elementList: PropTypes.array.isRequired,
    elementViewProvider: PropTypes.func.isRequired
};

export default SearchableViewList;