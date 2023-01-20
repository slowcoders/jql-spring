import axios from "axios";

const baseUrl = 'http://localhost:7007/api/jql/starwars/character';

const default_options = {
    select: "*",
    sort: '',
    limit: 0,
    page: 0,
}

function to_url_param(options) {
    if (!options) return "";

    let params = ""
    for (const k in options) {
        params += params.length > 1 ? '&' : '?';
        params += k + "=" + options[k];
    }
    return params;
}

async function call_http(method, command, filter, options) {
    //const params = to_url_param(options)
    const jql = { ...options, filter };
    const url = `${baseUrl}/${command}`
    const response = await axios[method].call(axios, url, jql, {
        headers: {
            "Content-Type" : "application/json"
        }
    });
    return response.data;
}

async function notifyPageChanged(paginationCallback, res) {
    if (res.pageable) {
        const pageSize = res.pageable.pageSize;
        const pageNumber = res.pageable.pageNumer;
        const totalElements = res.totalElements;
        paginationCallback(pageSize, pageNumber, totalElements);
    } else {
        pageSize = page.content.length;
        paginationCallback(pageSize, 0, pageSize);
    }
}

export const jqlApi = {
    cachedListTs: 0,
    cachedList: null,

    async count(filter) {
        return await call_http('post', 'count', filter)
    },

    async find(filter, options) {
        return await call_http('post', '', filter, options);
    },

    async top(filter, options) {
        options = { ...options, page: -1, limit: 1 }
        const res = await call_http('post', '', filter, options);
        console.log(res.content);
        return res.content.length > 0 ? res.content[0] : null;
    },
}

