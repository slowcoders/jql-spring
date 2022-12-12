
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Comment 를 해제하면서 Join 쿼리 사용법을 익혀본다. </H5>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  // "starship": {},
  // "+episode": {},

  // 아래 3개는 검색 조건하나 동등하나, 출력 결과에 차이가 있다.
  // "characterFriendLink.friend": {},
  // "characterFriendLink": { "friend": {} },
  // "+friend": {},
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
