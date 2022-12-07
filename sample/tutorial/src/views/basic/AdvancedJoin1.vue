
<template>
  <LI> Han Solo 의 친구 중 길이가 10m 이상인 우주선 비행사를 검색한다. </LI>
  <LessonView
      :js_code="code"
      :enable_table_select="false"
  />
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  "name" : "Han Solo",
  "+friend": {
    "starship": {
      "length@ge": 10
    }
  }
}
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
