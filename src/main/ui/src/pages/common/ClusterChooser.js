import React, {Component} from "react";
import "../../App.css";
import {Dropdown, DropdownItem, DropdownMenu, DropdownToggle} from "reactstrap";
import {AppDataContext} from "../../contexts/AppDataContext";
import SessionStore from "../../services/SessionStore";
import {toast} from "react-toastify";

class ClusterChooser extends Component {

    static contextType = AppDataContext

    constructor(props) {
        super(props);

        this.state = {
            clusterDropDownOpen: false,
            activeCluster: null,
            clusters: []
        };
    }

    componentDidMount() {
        this.mounted = true;
        SessionStore.getActiveCluster((activeCluster) => {
            SessionStore.getClusters((clusterList) => {
                if (this.mounted) {
                    this.setState({
                        clusters: clusterList
                    })
                }
            }, () => toast.error("Cannot get a list of broker addresses"));

            if (this.mounted) {
                this.setState({
                    activeCluster: activeCluster
                })
            }
        }, () => toast.error("Cannot get a list of broker addresses"));

    }

    toggleCluster = () => {
        this.setState({
            clusterDropDownOpen: !this.state.clusterDropDownOpen
        });
    };

    setActiveCluster = (cluster) => {
        this.setState({
            activeCluster: cluster
        }, () => {
            this.context.setActiveCluster(cluster);
            this.context.clearState();
        })
    };

    render() {
        return (
            <div>
                <Dropdown nav inNavbar size="sm" isOpen={this.state.clusterDropDownOpen} toggle={this.toggleCluster}>
                    <DropdownToggle nav caret>
                        Active Cluster: {this.state.activeCluster}
                    </DropdownToggle>
                    <DropdownMenu>
                        {
                            this.state.clusters.map((cluster) => (<DropdownItem key={cluster}
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