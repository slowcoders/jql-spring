# Json Query Language (JQL)

## what is JQL
* JSON 기반 계층적 Query 언어
* Front-end 개발자도 손쉽게 사용할 수 있는 단순한 API
* SQL 자동 변환 및 처리
* Java Persistence Api(JPA)를 통한 기능 확장.


## Grammar
JQL 은 JSON 과 문법적으로 동일하다. 단, object member name(=key) 을 다음과 같이 확장하여 사용한다.
```sh
jql_key = attribute_name '@' operator
```

attribute_name 은 논리적인 DB Column 명으로, JPA Entity의 Java 필드명에 해당한다.<br>
다음은 starwars.character table 에서 키가 2m 이상인 character 를 검색하는 예제이다.
```sh
curl -X 'POST' \
  'http://localhost:6090/api/jql/starwars/character/find' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
        "height@ge": 2.0
      }'
```

## JQL operators
### is, not
* SQL 문 '=', '!=' 에 해당, Join 쿼리 시엔 'in', 'not in' 에 해당한다. 'is' 연산자는 생략 가능하다.
```
{ "id" : 1000 }              /* --> where id = 1000 */ 
{ "id@is" : 1000 }           /* --> where id = 1000 */ 
{ "id@not" : 1000 }          /* --> where id != 1000 */ 
{ "id" : [1000, 1001]}       /* --> where id in (1000, 1001) */ 
{ "id@is" : [1000, 1001]}    /* --> where id in (1000, 1001) */ 
{ "id@not" : [1000, 1001]}   /* --> where id not in (1000, 1001) */ 
```
### like, not like
* SQL 문 'like', 'not like' 에 해당
```
{ "name@like" : "%e" }       /* --> where name like '%e%' */ 
{ "name@not like" : "%e" }   /* --> where name not like '%e%' */ 
```
### le, lt, ge, gt
* SQL 문 '<=', '<', '>=', '>' 에 해당
```
{ "id@lt" : 1000 }           /* --> where name <  1000 */ 
{ "id@le" : 1000 }           /* --> where name <= 1000 */ 
{ "id@gt" : 1000 }           /* --> where name >  1000 */ 
{ "id@ge" : 1000 }           /* --> where name >= 1000 */ 
```


## Automated JDBC Join
JQL-JDBC 구현체는 JDBC metadata 를 분석하여 foreign key 로 연결된 Table 간 join 관계를 자동 처리한다.<br>
아래는 Luke Skywalker 와 친한 Droid 들을 검색하는 예제이다.<br>
```json
{
  "name@like": "Luke%",
  "+friend": {
    "species": "Droid"
  }
}
```
결과
```json
[
  {
    "id": 1000,
    "name": "Luke Skywalker",
    ...
    "+friend": [
      {
        "id": 2000,
        "species": "Droid",
        "name": "C-3PO",
        "primaryFunction": "protocol",
        ...
      },
      {
        "id": 2001,
        "species": "Droid",
        "name": "R2-D2",
        "primaryFunction": "Astromech",
        ...
      }
    ]
  }
]
```
위 쿼리와 관계된 table 구조들 
```js
starwars.character = {
  id: bigint (pk)
  height: real
  home_planet: text
  mass: real
  name: text
  note: jsonb
  primary_function: text
  species: text
};

// assicative table
starwars.character_friend_link = {
  character_id: bigint (fk) -> starwars.character.id
  friend_id: bigint (fk) -> starwars.character.id
};
```
