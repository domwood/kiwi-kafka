import React, {Component} from "react";
import {Button, Container, Table} from "reactstrap";
import * as ApiService from "../../services/ApiService";
import {toast} from "react-toastify";
import "../../App.css";

const ONE_KILOBYTE = 1024;
const ONE_MEGABYTE = 1024 * 1024;

class KafkaHome extends Component {
    constructor(props) {
        super(props);

        this.state = {
            brokers: [],
            logFiles: [],
            activeNode: null
        };
    }

    componentDidMount() {
        this.mounted = true;
        ApiService.getBrokers((brokers) => {
            if (this.mounted) {
                this.setState({
                    brokers: brokers
                });
            }
        }, () => toast.error("Could not retrieve broker list from server"));
    }

    componentWillUnmount() {
        this.mounted = false;
    }

    logFiles = (id) => {
        ApiService.getLogs(id, logs => {
            this.setState({
                logFiles: logs,
                activeNode: id
            });
            toast.info("Retrieved Log files for broker " + id)
        }, () => toast.error("Could not retrieve Log files for broker " + id))
    };

    formatSize = (size) => {
        let round = (num) => Math.round(num * 100) / 100

        if (!size) {
            return "0 B"
        }
        if (size > ONE_KILOBYTE) {
            return `${round(size / ONE_KILOBYTE)} KB`;
        }
        if (size > ONE_MEGABYTE) {
            return `${round(size / ONE_MEGABYTE)} MB`;
        } else {
            return `${round(size)} B`;
        }
    }

    //TODO Improve display of this data
    render() {
        return (
            <Container className={"WideBoi"}>
                <div className="mt-lg-4"/>
                <h1>Kafka Broker Information</h1>
                <div className="mt-lg-4"/>

                {
                    this.state.brokers.length > 0 ?
                        <Table size="sm" bordered striped>
                            <thead>
                            <tr>
                                <th>Number</th>
                                <th>Name</th>
                                <th>Address</th>
                                <th>Assigned Rack</th>
                                <th>Log File Query</th>
                            </tr>
                            </thead>
                            <tbody className="WrappedTable">
                            {
                                this.state.brokers.map(m => {
                                    return (
                                        <tr key={m.nodeNumber}>
                                            <td>{m.nodeNumber}</td>
                                            <td>{m.nodeName}</td>
                                            <td>{m.host}:{m.port}</td>
                                            <td>{m.nodeRack || 'None'}</td>
                                            <td>
                                                <Button style={{"width": "100%"}} size="sm"
                                                        onClick={() => this.logFiles(m.nodeNumber)}>Show Log
                                                    Files</Button>
                                            </td>
                                        </tr>
                                    )
                                })
                            }
                            </tbody>
                        </Table> : ''
                }
                <div className="mt-lg-4"/>

                {
                    this.state.logFiles.length > 0 ?
                        <div>
                            <h3>Log Files for Broker {this.state.activeNode}</h3>
                            <div className={"Gap"}/>
                            <div className={"Gap"}/>
                        </div> : ''

                }
                {
                    this.state.logFiles.length > 0 ?
                        <Table size="sm" bordered>
                            <thead>
                            <tr>
                                <th>Log Name</th>
                                <th>Error</th>
                                <th>Topic</th>
                                <th>Partition</th>
                                <th>Lag</th>
                                <th>Reported Size</th>
                            </tr>
                            </thead>
                            <tbody className="WrappedTable">
                            {
                                this.state.logFiles.map(log => {
                                    let topicName = '';
                                    return log.topicInfoList.map((info, index) => {
                                            let lastTopicName = topicName;
                                            topicName = info.topic;
                                            return (
                                                <tr key={info.topic + "_" + info.partition}>

                                                    <td width={"20%"}>{index === 0 ? log.logName : ''}</td>
                                                    <td width={"10%"}>{index === 0 ? log.errorType : ''}</td>
                                                    <td>{lastTopicName === topicName ? '' : info.topic}</td>
                                                    <td>{info.partition}</td>
                                                    <td>{info.lag}</td>
                                                    <td>{this.formatSize(info.size)}</td>
                                                </tr>
                                            )
                                        }
                                    )
                                })
                            }
                            </tbody>
                        </Table> : ''
                }

            </Container>

        );
    }
}

export default KafkaHome;