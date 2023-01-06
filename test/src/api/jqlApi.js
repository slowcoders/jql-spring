import axios from "axios";

const baseUrl = 'https://localhost:6090/jql/api/starwars';

export const jqlApi = {
    cachedListTs: 0,
    cachedList: null,

    async find(jql) {
        const url = `${baseUrl}/find?select=*`
        const response = await axios.post(url, jql);
        return response.data; 
    },

    async setCompleted(todoId, isCompleted) {
        const response = await axios.put(`${baseUrl}/${todoId}`, {
            completed: isCompleted
        }); 
        return response.data; 
    },
}

