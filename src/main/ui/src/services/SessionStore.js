import Cookies from 'universal-cookie';

const cookies = new Cookies();

const SessionStore = {
    setActiveCluster: (newCluster) => {
        let previousCluster = cookies.get("activeCluster");
        cookies.set("activeCluster", newCluster);
        if(previousCluster && newCluster && previousCluster !== newCluster){
            window.location.reload();
        }
    },
    getActiveCluster: () => cookies.get("activeCluster") || null
};

export default SessionStore;