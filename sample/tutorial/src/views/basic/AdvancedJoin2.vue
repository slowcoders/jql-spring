
<template>
  <LI> 특정 Episode 출연자의 친구의 친구 중 동일한 Episode 출연한 캐릭터 검색. </LI>
  <LessonView
      :js_code="code"
      :enable_table_select="false"
  />
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  "+episode" : {
      "title" : "JEDI"
  },
  "+friend<name>": {
    "+friend<name>": {
      "+episode" : {
          "title" : "JEDI"
      },
    }
  },
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
