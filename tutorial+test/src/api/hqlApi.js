import axios from "axios";

export function to_url_param(options) {
    if (!options) return "";

    let params = ""
    for (const k in options) {
        params += params.length > 1 ? '&' : '?';
        params += k + "=" + options[k];
    }
    return params;
}

const http_options = {
    headers: {
        "Content-Type" : "application/json"
    }
}

export class HqlApi {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    async count(filter) {
        const url = `${this.baseUrl}/count`
        filter = filter ? filter : {}
        const response = await axios.post(url, filter, http_options);
        return response.data;
    }

    async find(filter, options) {
        const url = `${this.baseUrl}/nodes${to_url_param(options)}`
        filter = filter ? filter : {}
        const response = await axios.post(url, filter, http_options);
        return response.data;
        // if (!content) {
        //     return response.data;
        // }
        // if (metadata) {
        //     content.$ = metadata;
        // }
        // return content;
    }

    async insert(entity, conflictPolicy) {
        const url = `${this.baseUrl}?select=*${conflictPolicy ? "&onConflict=" + conflictPolicy : "" }`
        const response = await axios.put(url, entity, http_options);
        return response.data;
    }

    async updateByIdList(idList, jql) {
        let res = await axios.patch(this.baseUrl+`/${idList}`, jql);
        return res.data.content;
    }


    async insertAll(entity, conflictPolicy) {
        const url = `${this.baseUrl}/add-all?select=*${conflictPolicy ? "&onConflict=" + conflictPolicy : "" }`
        const response = await axios.put(url, entity, http_options);
        return response.data;
    }

    async delete(idList) {
        const url = `${this.baseUrl}/${idList}`
        const response = await axios.delete(url, http_options);
        return response.data;
    }


    async top(filter, options) {
        options = { ...options, page: -1, limit: 1 }
        const data = await this.find(filter, options);
        return data.content.length > 0 ? data.content[0] : null;
    }
}

export class HqlForm {
    static hidden(key) {
        return {
            type: 'textfield',
            key: key,
            input: false
        }
    }
    static text(key, label, input=true) {
        return {
            type: 'textfield',
            key: key,
            label: label,
            placeholder: label,
            defaultValue: null,
            input,
        }
    }
    static number(key, label, input=true) {
        return {
            type: 'number',
            key: key,
            label: label,
            placeholder: label,
            input,
        }
    }
    static date(key, label, input=true) {
        return {
            type: 'datetime',
            key: key,
            label: label,
            placeholder: label,
            defaultValue: null,
            input,
            enableTime: false,
        }
    }
    static select(key, label, ref, input=true) {
        return {
            type: 'select',
            key: key,
            label: label,
            placeholder: label,
            input,
            widget: "choicesjs",
            dataRef: ref
        }
    }
    static tags(key, label, tags, input=true) {
        return {
            type: 'select',
            multiple: true,
            key: key,
            label: label,
            placeholder: label,
            input,
            widget: "choicesjs",
            data: { values : tags.map(tag => ({value: tag, label: tag})) }
        }
    }
}
