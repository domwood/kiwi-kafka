const baseUrl = process.env.REACT_APP_LOCAL_SPRING_API ? process.env.REACT_APP_LOCAL_SPRING_API : 'api/';

export default {
    listTopics: baseUrl + "listTopics",
    produce: baseUrl + "produce"
}