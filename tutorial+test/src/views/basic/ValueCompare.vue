
<template>
  <LessonView
      :js_code="code"
      target_table="customer">
    <template v-slot:description>
      <H5> 비교 연산자</H5>
      <div class="details">
        JQL 은 아래의 형태로 비교 연산식을 표현한다.<br>
        <b>"propertyKey 연산자" : 비교값</b><p/>
        "연산자" 부분이 생략되면 equals 연산을 수행한다.<br>
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const hql_select = AUTO;

/* 아래의 주석을 한 줄씩 번갈아 해제하면서 검색 결과의 차이를 비교해 보십시오. */
const hql_filter = {
  // "id": 1001
  // "id !=": 1001

  // "id <=": 1001
  // "id <": 1001
  // "id >=": 1004
  // "id >": 1004

  // "id >=": 1001, "id <=": 1002
  // "id between": [1001, 1003]
  // "id !between": [1001, 1003]

  // "name": "Luke Skywalker"
  // "name !=": "Luke Skywalker"

  // "name <=": "Han Solo"
  // "name <": "Han Solo"
  // "name >=": "Han Solo"
  // "name >": "Han Solo"

  // "name >=": "Darth Vader", "name <=": "Han Solo"
  // "name between": ["Darth Vader", "Hobbit"]
  // "name !between": ["Darth Vader", "Hobbit"]

  // "name like": "L%"
  // "name !like": "L%"
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
