import axios from "axios";

const baseUrl = 'http://localhost:6090/api/jql/starwars/character';

async function call_http(method, command, jql, select, sort, limit) {
    if (!select) select = '*'
    if (!sort) sort = ''
    if (!limit) limit = '-1'
    const url = `${baseUrl}/${command}?select=${select}&sort=${sort}&limit=${limit}`
    const response = await axios[method].call(axios, url, jql);
    console.log(url, response.data);
    return response.data;
}
export const jqlApi = {
    cachedListTs: 0,
    cachedList: null,

    async find(jql, select, sort, limit) {
        return await call_http('post', 'find', jql, select, sort, limit)
        // let method = 'post'
        // let command = 'find'
        // if (!select) select = '*'
        // if (!sort) sort = ''
        // if (!limit) limit = '-1'
        // const url = `${baseUrl}/${command}?select=${select}&sort=${sort}&limit=${limit}`
        // console.log(url);
        // let response;
        // switch (method) {
        //     case 'post':
        //     default:
        //         response = await axios.post(url, jql);
        //         break;
        // }
        // return response.data;
    },

    async listAll(jql, select, sort, limit) {
        return await call_http('post', '', jql, select, sort, limit)
    },

    async top(jql, select, sort) {
        return await call_http('post', 'top', jql, select, sort, 1)
    },
}

