
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> 키가 1.5m 이하이거나 체중이 100kg 이상인 Character 검색 방법(1) </H5>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  "@in": [
    { "height@le": 1.5 },
    { "mass@gt": 100 },
  ]
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
