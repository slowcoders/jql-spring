
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> 특정 Episode 출연자의 친구의 친구 중 동일한 Episode 출연한 캐릭터 검색. </H5>
      <div class="details">
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  /*
   Associative Table 의 두 개의 Foreign key 가 동일한 PK-Table 을 참조하는 경우,
   PK-Table 명을 포함한 Foreign key 와 PK-Table 의 Private-key 를 자동 Join 한다.
   Joined-Foreign Key 를 제외한 나머지 Foreign Key 에 Join 된 Entity 는 '+' 가상 칼럼으로 접근할 수 있다.
   아래는 StarWars 캐릭터 중 Luke 와 친구 관계인 캐릭터들을 검색한다.
  */
  // "name@like": "Luke%", "characterFriendLink": { "friend": {} }
  // "name@like": "Luke%", "characterFriendLink.friend": {}
  // "name@like": "Luke%", "+friend": {}

  "+friend<name>": {
    "+friend<name>": {
      "+episode" : {
          "title" : "JEDI"
      },
    }
  },
}
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
