import url from "./ApiConfig";


export const getTopics = (cb) => {
    console.log(url);
    console.log(process.env);

    cb(["activeStateFeed", "sportsIncidentFeed", "aggregatedFixtures"]);
};

