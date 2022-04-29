import React, {Component} from "react";
import "../../App.css";
import {Dropdown, DropdownItem, DropdownMenu, DropdownToggle} from "reactstrap";
import {AppDataContext} from "../../contexts/AppDataContext";

class ClusterChooser extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);

        this.state = {
            clusterDropDownOpen: false
        };
    }

    componentDidMount() {
        this.mounted = true;
    }

    toggleCluster = () => {
        this.setState({
            clusterDropDownOpen: !this.state.clusterDropDownOpen
        });
    };

    setActiveCluster = (cluster) => {
        this.context.setActiveCluster(cluster);
    };

    render() {
        return (
            <div>
                <Dropdown nav inNavbar size="sm" isOpen={this.state.clusterDropDownOpen} toggle={this.toggleCluster}>
                    <DropdownToggle nav caret>
                        Active Cluster: {this.context.activeCluster}
                    </DropdownToggle>
                    <DropdownMenu>
                        {
                            this.context.clusters.map((cluster) => (<DropdownItem key={cluster}
                                                                                  onClick={() => this.setActiveCluster(cluster)}>{cluster}</DropdownItem>))
                        }
                    </DropdownMenu>
                </Dropdown>
            </div>
        )
    }
}

ClusterChooser.propTypes = {};

export default ClusterChooser;