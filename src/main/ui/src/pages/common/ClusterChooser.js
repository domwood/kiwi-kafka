import React, {Component} from "react";
import PropTypes from "prop-types";
import "../../App.css";
import {Dropdown, DropdownItem, DropdownMenu, DropdownToggle} from "reactstrap";
import * as ApiService from "../../services/ApiService";
import {toast} from "react-toastify";

class ClusterChooser extends Component {

    constructor(props) {
        super(props);

        this.state = {
            clusterDropDownOpen: false
        };

    }
    componentDidMount() {
        this.mounted = true;

        ApiService.getKafkaConfiguration((kafkaConfig) => {
            if(this.mounted){
                this.setState({
                    kafkaConfig: kafkaConfig,
                    activeCluster: Object.keys(kafkaConfig || {"none":null})[0]
                })
            }
        }, () => toast.error("No connection to server"));
    }

    toggleCluster = () => {
        this.setState({
            clusterDropDownOpen: !this.state.clusterDropDownOpen
        });
    };

    setActiveCluster = (cluster) => {
        this.setState({
            activeCluster: cluster
        });
    };

    render() {
        return (
            <div style={{padding:0.25 +'rem'}}>
                <Dropdown size="sm" isOpen={this.state.clusterDropDownOpen} toggle={this.toggleCluster} onClick={() => {}}>
                    <DropdownToggle caret>
                        Active Cluster: {this.state.activeCluster}
                    </DropdownToggle>
                    <DropdownMenu>
                        <DropdownItem onClick={() => this.setActiveCluster("default")}>default</DropdownItem>
                        <DropdownItem onClick={() => this.setActiveCluster("Other Cluster")}>Other Cluster</DropdownItem>
                    </DropdownMenu>
                </Dropdown>
            </div>
        )
    }
}

ClusterChooser.propTypes = {
    onUpdate: PropTypes.func
};

export default ClusterChooser;