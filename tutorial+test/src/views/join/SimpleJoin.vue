
<template>
  <LessonView
      :js_code="code"
      target_table="customer">
    <template v-slot:description>
      <H5> Join Query. </H5>
      <div class="details">
        JQL 은 DB 의 metadata 를 분석하여 FK->PK Join 관계를 자동 분석하여 처리한다.<br>
        Property key 를 '.' 기호로 연결하거나, 비교값 위치에 Object {} 또는 Object Array [ {} ] 를 사용하여 Joined Query 를 작성할 수 있다.<p/>
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const hql_select = "name, book_.title";

const hql_filter = {

  /* 특정 도서를 구입한 고객 검색.
  ----------------------------------------------------------*/
  // "book_": { "title": "소년이 온다" }

  /* 특정 저자의 도서를 구입한 고객 검색.
  ----------------------------------------------------------*/
  // "book_": { "author.name": "한강" }

  /* 특정 가격 이상의 도서를 구입한 고객 검색.
  ----------------------------------------------------------*/
  // "book_": { "price >=": "15000" }

  /* '{' '}' 내부의 비교식은 And 조건으로 처리된다. 아래 두 예제는 각각
    특정 가격 이상이고, 제목에 '소년'이 포함된 도서를 구입한 고객을 검색한다.
  ----------------------------------------------------------*/
  // "book_.price >=": 12000, "book_.title like": "%소년%"
  // "book_": { "price >=": 12000, "title like": "%소년%" }

  /* [ ] 내부의 비교 노드는 Or 조건으로 처리된다.
     특정 가격 이하 또는 특정 가격 이상의 도서를 구입한 고객을 검색한다.
  ----------------------------------------------------------*/
  // "book_": [ { "price <=": 10000 }, { "price >=": 15000 } ]

  /* [ ] 내부의 비교 노드는 Or 조건으로 처리된다.
    특정 가격 이하이거나, 제목에 '소년'이 포함된 도서를 구입한 고객을 검색한다.
  ----------------------------------------------------------*/
  // "book_": [ { "price >=": 12000 }, { "title like": "%소년%" } ]
}
`

export default {
  components: {
    LessonView
  },

  data() {
    return {
      code: sample_code
    }
  }
}
</script>
