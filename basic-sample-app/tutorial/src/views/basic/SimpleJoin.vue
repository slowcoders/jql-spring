
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

  // 'character_episode_link' table 은 character_id, episode_id 두 개의 Foreign-key 만 가진 Associative Table 이다.
  // 가상 칼럼을 이용하여 Associative Table 을 통해 연결된 다른 Entity 에 대한 Join 을 보다 쉽게 처리할 수 있다.
  // (가상 칼럼은 '+' 기호로 시작한다.)
  // 아래는 JEDI 에피소드에 출연한 출연진을 검색한다.
  // 아래 5개의 검색 조건은 동등하나, 출력 결과에 차이가 있다.
  // "characterEpisodeLink": { "episode": { "title": "JEDI" } }
  // "characterEpisodeLink.episode": { { "title": "JEDI" } }
  // "characterEpisodeLink.episode.title": "JEDI"
  // "+episode": { "title": "JEDI" }
  // "+episode.title": "JEDI"

  // Associative Table 의 두 개의 Foreign key 가 동일한 PK-Table 을 참조하는 경우,
  // PK-Table 명을 포함한 Foreign key 와 PK-Table 의 Private-key 를 자동 Join 한다.
  // Joined-Foreign Key 를 제외한 나머지 Foreign Key 에 Join 된 Entity 는 '+' 가상 칼럼으로 접근할 수 있다.
  // 아래는 StarWars 캐릭터 중 Luke 와 친구 관계인 캐릭터들을 검색한다.
  // "name@like": "Luke%", "characterFriendLink": { "friend": {} }
  // "name@like": "Luke%", "characterFriendLink.friend": {}
  // "name@like": "Luke%", "+friend": {}

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
