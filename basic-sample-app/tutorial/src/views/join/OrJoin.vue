
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Join 한 Entity 에 대한 Or 조건 검색 </H5>
      * JQL 문법은 { } 내부는 And 연산, [ ] 내부는 Or 연산을 기본 적용한다.<br>
      Join 할 Entity 검색 조건을 [ ] 로 묶으면 Or 로 처리한다. <br>
      아래는 JEDI 또는 NEWHOPE 에피소드 출연자 중 5m 이상의 비행선의 조종사를 검색하는 예제이다.<p/>
      <p/>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  "+episode": [
    { "title": "JEDI" },
    { "title": "NEWHOPE" }
  ],
  "starship": {
    "length@ge": 5
  }
}
this.http_post(\`http://localhost:6090/api/jql/\${dbSchema}/\${dbTable}/find?sort=\${sort}&limit=\${limit}\`, jql);
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
