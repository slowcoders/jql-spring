# Json Query Language (JQL)

## What is JQL
* JSON based hierarchical Query language
* Simple query language that can be easily used by frond-end developers.
* GraphQL like automatic property selection 

## What is JQL for JDBC
* Automatic table join.
* JPA-like query result without ORM layer.
* Fully customizable via JPA, MyBatis, JDBC.


## JQL Grammar
```sh
jql = {
    select: "<jql_property_selection>",
    sort: "<jql_sort_option>",
    limit: Integer,
    page: Integer,
    filter: jql_node
}
jql_key = Identifier['.'jql_key]
jql_relative_selector = '*' | '0' | jql_key
jql_relative_selector_set = jql_key '.<' jql_relative_selector { ',' jql_relative_selector } '>'
jql_property_selection = [ (jql_relative_selector | jql_relative_selector_set) [',' jql_property_selection ] ]  
jql_sort_option = [-]property_name [',' jql_sort_option]

jql_node = '{' jql_predicates [',' jql_node] '}'
jql_predicates = '"' jql_key ['@' jql_operator] '"' : jql_value
jql_operator = "is" | "not" | "like" | "not like" | "lt" | "gt" | "le" | "ge" | "between" | "not between"
jql_primitive = false | null | true | number | string
jql_value = jql_primitive | ArrayOf(jql_primitive) | jql_node | ArrayOf(jql_node)  
```


## Query Examples
Finding all person whose first name starts with "Jeff" 
```js
const DB_TABLE = "owner"
const jql = {
    filter: {
        "name@like": "Jeff%"
    }
}
const res = axios.post(`http://localhost:7007/api/jql/petclinic/${DB_TABLE}/find`, jql)
```
```sh
curl -X 'POST' 'http://localhost:7007/api/jql/petclinic/owners/find' \
     -H 'Content-Type: application/json' \   
     -d '{  "filter": { "name@like": "Jeff%" } }'
```

Finding all cats of the person named "Jeff Black"
```js
const DB_TABLE = "pets"
const jql = {
    select: "*",
    filter: {
        "type": "cat",
        "owner": {
            "name": "Jeff Black"
        }
    }
}
const res = axios.post(`http://localhost:7007/api/jql/petclinic/${DB_TABLE}/find`, jql)
```
```sh
curl -X 'POST' 'http://localhost:7007/api/jql/petclinic/pets/find' \
     -H 'Content-Type: application/json' \   
     -d '{ "filter": { "name": "Jeff Black", "pets": { "type": "cat" } }'
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
{ "starship" : { id: 3000 } } 
```
    --> SELECT t_0.*, t_1.* FROM starwars.character as t_0
        left join starwars.starship as t_1 on
        t_0.id = t_1.pilot_id
        WHERE (t_1.id = 3000)
```

{ "starship" : [ { id: 3000 }, { id: 3001 } ] }           
```
    --> SELECT t_0.*, t_1.* FROM starwars.character as t_0
        left join starwars.starship as t_1 on
        t_0.id = t_1.pilot_id
        WHERE (t_1.id = 3000 or t_1.id = 3001) */
```


