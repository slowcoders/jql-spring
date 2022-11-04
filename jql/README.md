# Json Query Language (JQL)

## what is JQL
JQL 은 JSON 기반의 Data Query 언어이다. <br>
JQL 은 GraphQL 과 유사한 기능을 제공하며, 내장 Operator 를 이용하여 검색 조건을 손쉽게 설정할 수 있다.<br>
아래는 서울 지역에 사는 '김' 씨 성을 가진 사람들을 검색하는 JQL 예제이다.<br>
```sql
{
    "author": "김"
    "actor": {
        "lastName": "Seoul"
    }
}
```
GraphQL 과 달리 JQL 는 RDB 환경에 쉽게 적용할 수 있도록 설계 되었으며, 연산자를 이용하여 검색 조건을 쉽게 구체화할 수 있도록 하였다<br>

## JQL Operator
### eq, ne
SQL 문 '=', '!=' 에 해당 (eq 연산자는 생략 가능)
아래는 서울 지역에 살지 않는 '김' 씨 성을 가진 사람들을 검색하는 JQL 예제이다.<br>
```sql
{
    "author": "김"
    "address": {
        "city@ne": "Seoul"
    }
}
```

### le, lt, ge, gt
SQL 문 '<=', '<', '>=', '>' 에 해당
### in, not in
SQL 문 'in', 'not in' 에 해당 
### like, not like
SQL 문 'like', 'not like' 에 해당 

## JQL Value
### 객체 Notation '{}' 
Entity Join 을 위해 사용된다. 
