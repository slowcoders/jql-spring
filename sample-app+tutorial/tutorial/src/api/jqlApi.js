import axios from "axios";

const baseUrl = 'http://localhost:6090/api/jql/starwars/character';

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

async function call_http(method, command, jql, options) {
    const params = to_url_param(options)
    const url = `${baseUrl}/${command}${params}`
    const response = await axios[method].call(axios, url, jql);
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

    async count(jql, options) {
        return await call_http('post', 'count', jql ?? {}, options)
    },

    async list(options, paginationCallback) {
        return await call_http('get', '', null, options)
    },

    async find(jql, options) {
        return await call_http('post', 'find', jql ?? {}, options);
    },

    async top(jql, options) {
        jql = jql ?? {}
        return await call_http('post', 'top', jql ?? {}, options)
    },
}

