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
            });
        }
    }

    filterList = (filterWord) => {
        if (filterWord && filterWord.length > 0) {
            this.setState({
                filteredList: this.state.unfilteredList
                    .filter(element => element.toLowerCase().search(filterWord.toLowerCase()) !== -1)
            })
        } else {
            this.setState({
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
                            onChange={selected => selected ? this.filterList(selected[0]) : ''}
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