
<template>
  <LI> Comment 를 각각 해제하면서 operator 사용법을 익혀본다. </LI>
  <LessonView
      :js_code="code"
      :enable_table_select="false"
  />
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  // "id@le": 1001
  // "id@lt": 1001
  // "id@ge": 1001
  // "id@gt": 1001
  // "id@between": [1001, 1002]
  // "id@not between": [1001, 1002]
  // "id@ge": 1001, "id@le": 1002
  // "name@like": "Luke%"
  // "nema@not like": "Luke%"
  // "name@like": [ "Luke%", "Darth%" ]
  // "name@not like": [ "Luke%", "Darth%" ]
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
