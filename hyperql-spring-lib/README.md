# Json Query Language (JQL)

## What is JQL
* Hierarchical property selection like GraphQL
* Simple search filter with Json syntax

## JQL for JDBC
* Automatic repository creation
* Automatic join query generation.
* Supports JPA EntityManager.


## JQL Grammar
```sh
hql = {
    select: "<hql_property_selection>",
    sort: "<hql_sort_option>",
    limit: Integer,
    page: Integer,
    filter: hql_node
}
hql_key = Identifier['.'hql_key]
hql_selector = '*' | '0' | hql_key
hql_selector_set = hql_key '.(' hql_selector { ',' hql_selector } ')'
hql_property_selection = [ (hql_selector | hql_selector_set) [',' hql_property_selection ] ]  

hql_sort_option = [-]hql_key [',' hql_sort_option]

hql_node = '{' hql_predicates [',' hql_node] '}'
hql_predicates = '"' hql_key ['@' hql_operator] '"' : hql_value
hql_operator = "is" | "not" | "like" | "not like" | "lt" | "gt" | "le" | "ge" | "between" | "not between"
hql_primitive = false | null | true | number | string
hql_value = hql_primitive | ArrayOf(hql_primitive) | hql_node | ArrayOf(hql_node)  
```


## Query Examples
Finding all person whose first name starts with "Luke" 
```js
const DB_TABLE = "student"
const hql = { 
    "name@like": "Luke%"
}
const res = axios.post(`http://localhost:7007/api/hql/bookstore/${DB_TABLE}/find`, hql)
```
```sh
curl -X 'POST' 'http://localhost:7007/api/hql/bookstore/student/find' \
     -H 'Content-Type: application/json' \
     -d '{ "name@like": "Luke%" }'
```

Finding all books of the person named "Luke Skywalker"
```js
const DB_TABLE = "book"
const hql = {
    select: "*",
    filter: {
        "student": {
            "name": "Luke Skywalker"
        }
    }
}
const res = axios.post(`http://localhost:7007/api/hql/bookstore/${DB_TABLE}/find`, hql)
```
```sh
curl -X 'POST' 'http://localhost:7007/api/hql/bookstore/book/find' \
     -H 'Content-Type: application/json' \
     -d '{ "filter": { "student": { "name": "Luke Skywalker" } } }' 
```
## JQL operators vs SQL
```
{ "id" : 1000 }              /* --> where id = 1000 */ 
{ "id@is" : 1000 }           /* --> where id = 1000 */ 
{ "id@not" : 1000 }          /* --> where id != 1000 */ 
{ "id" : [1000, 1001]}       /* --> where id in (1000, 1001) */ 
{ "id@is" : [1000, 1001]}    /* --> where id in (1000, 1001) */ 
{ "id@not" : [1000, 1001]}   /* --> where id not in (1000, 1001) */ 
```

### like, not like
```
{ "name@like" : "%e" }       /* --> where name like '%e%' */ 
{ "name@not like" : "%e" }   /* --> where name not like '%e%' */ 
{ "name@like" : ["%e", "%f"] }       /* --> where name like '%e%' or like '%f%' */ 
{ "name@not like" : ["%e", "%f"] }   /* --> where name not (like '%e%' or like '%f%') */
```

### le, lt, ge, gt, between, not between 
```
{ "id@lt" : 1000 }                      /* --> where id <  1000 */ 
{ "id@le" : 1000 }                      /* --> where id <= 1000 */ 
{ "id@gt" : 1000 }                      /* --> where id >  1000 */ 
{ "id@ge" : 1000 }                      /* --> where id >= 1000 */ 
{ "id@between" : [1000, 1001] }         /* --> where id >= 1000 and id <= 1001 */ 
{ "id@not between" : [1000, 10001] }    /* --> where not (id >= 1000 and id <= 1001) */ 
```

### Automatic table join with JQL.
{ "book" : { id: 3000 } } 
```
    --> SELECT t_0.*, t_1.* FROM bookstore.student as t_0
        left join bookstore.book as t_1 on
        t_0.id = t_1.student_id
        WHERE (t_1.id = 3000)
```

{ "book" : [ { id: 3000 }, { id: 3001 } ] }           
```
    --> SELECT t_0.*, t_1.* FROM bookstore.student as t_0
        left join bookstore.book as t_1 on
        t_0.id = t_1.student_id
        WHERE (t_1.id = 3000 or t_1.id = 3001) */
```


