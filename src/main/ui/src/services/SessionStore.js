//TODO 

const SessionStoreData = {
    activeCluster: 'none',
};

const SessionStore = {
    updateActiveCluster: (cluster) => {
        SessionStoreData.activeCluster = cluster
    },
    getActiveCluster: () => SessionStoreData.activeCluster
};

export default SessionStore;