
<template>
  <LI> Comment 를 해제하면서 Join 쿼리 사용법을 익혀본다. </LI>
  <LessonView
      :js_code="code"
      :enable_table_select="false"
  />
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  // "+starship": {},
  // "+friend": {},
  // "+episode": {},
}
this.http_post(\`http://localhost:6090/api/jql/\${dbSchema}/\${dbTable}/find?sort=\${sortBy}&limit=\${limit}\`, jql);
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
