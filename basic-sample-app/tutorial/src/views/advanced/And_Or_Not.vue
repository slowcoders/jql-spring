
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Join 한 Entity 에 대한 And/Or 조건 검색 </H5>
      * { } 내부는 And 연산, [ ] 내부는 Or 연산이 적용된다.
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
