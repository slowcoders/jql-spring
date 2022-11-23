# Json Query Language (JQL)

## what is JQL
JQL 은 JSON 기반의 Hierarchical Data Query 언어이다. <br>
JQL 은 GraphQL 과 유사한 기능을 제공하며, 내장 Operator 를 이용하여 검색 조건을 손쉽게 설정할 수 있다.<br>
JQL 은 RDB 환경에 쉽게 적용할 수 있도록 설계 되었으며, 연산자를 이용하여 검색 조건을 쉽게 구체화할 수 있도록 하였다<br>

## Tutorial 실행 준비.
```
cd 
sh run_postgres.sh
```

## Grammar
JQL 은 JSON 과 문법적으로 동일하다. 단, object member name(=key) 을 다음과 같이 확장하여 사용한다.
jql_key = attribute_name '@' operator

attribute_name 은 논리적인 DB Column 명에 해당한다.
다음은 starwars.character table 에서 키가 2m 이상인 character 를 검색하는 예제이다.
```
curl -X 'POST' \
  'http://localhost:6090/api/jql/starwars/character/find' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
        "height@le": 10
      }'
```
primitive_value := true | false | null | number | string
jql_object := '{' jql_key ':' jql_value '}'
array_value := '[' primitive_value *(',' primitive_value) ']'
json_value = false | null | true | jql_object / value_array / number / string
jql := '{' [ jql_key ':' json_value | jql | array_start json_value , array_end | '[' jql ']' ] }

## JQL operators
### eq, ne
* SQL 문 '=', '!=' 에 해당
### like, not like
* SQL 문 'like', 'not like' 에 해당
### le, lt, ge, gt
* SQL 문 '<=', '<', '>=', '>' 에 해당
### in, not in
* SQL 문 'in', 'not in' 에 해당, json array 를 인자로 받는다. 


## Automated Join
JQL-JDBC 구현체는 JDBC metadata 를 분석하여 Table 간 join 관계를 자동 처리한다.<br>
아래는 Luke Skywalker 를 돕는 Droid 들을 검색하는 예제이다.
```json
{
  "name@like": "Luke%",
  "+friend": {
    "dtype": "Droid"
  }
}
```
결과
```json
[
  {
    "id": 1000,
    "dtype": "Human",
    "name": "Luke Skywalker",
    "primaryFunction": null,
    "height": 1.72,
    "homePlanet": "Tatooine",
    "mass": 77,
    "+friend": [
      {
        "id": 2000,
        "dtype": "Droid",
        "name": "C-3PO",
        "primaryFunction": "protocol",
        "height": null,
        "homePlanet": null,
        "mass": null
      },
      {
        "id": 2001,
        "dtype": "Droid",
        "name": "R2-D2",
        "primaryFunction": "Astromech",
        "height": null,
        "homePlanet": null,
        "mass": null
      }
    ]
  }
]
```

