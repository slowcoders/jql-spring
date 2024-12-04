# Hyper Query Language (HQL)

## What is HQL
* Bidirectional graph data search filter using Json syntax
* Seamless RDB and json data intergration

## HQL for JDBC
* Automatic repository creation
* Automatic join query generation.
* Supports JPA EntityManager.


## HQL Grammar
```sh
hql = {
    select: "<hql_selector>",
    sort: "<hql_sort_option>",
    limit: Integer,
    page: Integer,
    filter: hql_node
}
hql_key = Identifier['.' hql_key]
hql_selector = '*' | '0' | hql_key [('.(' hql_selector ')') | (',' hql_selector)]
hql_sort_option = ['+' | '-'] hql_key [',' hql_sort_option]

hql_node = '{' (hql_conjunction | hql_predicates) [',' hql_node] '}'
hql_conjunction = ('and' | 'or' | 'not') ':' (hql_node | ArrayOf(hql_node))
hql_predicates = (hql_key [' ' hql_operator]) ':' hql_value
hql_operator = ['!'] ('==' | 'like' | 'like' | '<' | '>' | '<=' | '>=' | 'between' | 'contains' | 'overlaps') ['*']
hql_primitive = boolean | number | string
hql_value = null | hql_primitive | ArrayOf(hql_primitive) | 
```


## Query Examples
Finding all person whose first name starts with "Luke" 
```js
const DB_TABLE = "customer"
const hql = { 
    "name like": "Luke%"
}
const res = axios.post(`http://localhost:7007/api/hql/bookstore/${DB_TABLE}/nodes`, hql)
```
```sh
curl -X 'POST' 'http://localhost:7007/api/hql/bookstore/customer/nodes' \
     -H 'Content-Type: application/json' \
     -d '{ "name like": "Luke%" }'
```

Finding all books of the person named "Luke Skywalker"
```js
const DB_TABLE = "book"
const hql = {
    select: "*",
    filter: {
        "customer": {
            "name": "Luke Skywalker"
        }
    }
}
const res = axios.post(`http://localhost:7007/api/hql/bookstore/${DB_TABLE}/nodes`, hql)
```
```sh
curl -X 'POST' 'http://localhost:7007/api/hql/bookstore/book/nodes' \
     -H 'Content-Type: application/json' \
     -d '{ "filter": { "customer": { "name": "Luke Skywalker" } } }' 
```



