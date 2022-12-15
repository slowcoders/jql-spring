
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Han Solo 의 친구 중 길이가 10m 이상인 우주선을 조종하는 친구를 검색한다. </H5>
    </template>
  </LessonView>
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
  /*
   아래는 StarWars 캐릭터 중 Luke 와 친구 관계인 캐릭터들을 역방향으로 검색한다.
  */
  // "characterFriendLink": { "friend@in": { "name@like": "Luke%" } }
  // "characterFriendLink.friend@in": { "name@like": "Luke%" }
  // "+friend.name@like": "Luke%"

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
