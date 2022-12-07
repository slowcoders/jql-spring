<template>
  <LI> 전체 검색 </LI>
  <LessonView
      :js_code="code"
      :enable_table_select="true"
  />
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {}
this.http_post(\`http://localhost:6090/api/jql/\${dbSchema}/\${dbTable}/find?select=\${columns}&limit=\${limit}\`, jql);
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
