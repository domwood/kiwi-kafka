import Cookies from 'universal-cookie';
import api from "./ApiConfig";

const cookies = new Cookies();
const defaultHeaders = {
    sameSite: "None",
    secure: true
};

let cluster = null;
let clusters = [];
let pendingCbs = [];
let pendingEbs = [];
let pending = false;

const getKafkaClusterList = (cb, eb) => {
    let errorHandler = (error) => (eb || errorHandler)(error);

    fetch(api.kafkaClusterList)
        .then(response => {
            if (response.ok) {
                return response;
            } else {
                throw new Error(`${response.status} Error response from Server`);
            }
        })
        .then(res => res.json())
        .then(result => cb(result))
        .catch(errorHandler);
};


const SessionStore = {
    setActiveCluster: (newCluster) => {
        cluster = newCluster;
        cookies.set("activeCluster", newCluster, defaultHeaders);
    },
    getActiveCluster: (cb, eb) => {
        cluster = cookies.get("activeCluster") || cluster;
        if (!cluster) {
            if (pending) {
                pendingCbs.push(cb);
                pendingEbs.push(eb);
            } else {
                pending = true;
                getKafkaClusterList((clusterList) => {
                    cluster = clusterList[0];
                    clusters = clusterList;
                    cookies.set("activeCluster", cluster, defaultHeaders);
                    cb(cluster);
                    while (pendingCbs.length > 0) {
                        pendingCbs.pop()(cluster);
                    }
                    pending = false;
                }, (err) => {
                    eb(err);
                    while (pendingEbs.length > 0) {
                        pendingEbs.pop()(err);
                    }
                    pending = false;
                });
            }

        } else {
            cb(cluster);
        }
    },
    getClusters: (cb, eb) => {
        if (clusters.length === 0) {
            getKafkaClusterList((clusterList) => {
                clusters = clusterList;
                cb(clusterList);
            }, eb);
        } else return cb(clusters);
    }
};

export default SessionStore;