
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Join Query. </H5>
      <div class="details">
        JQL 은 DB 의 metadata 를 분석하여 FK->PK Join 관계를 자동 분석하여 처리한다.<br>
        Property key 를 '.' 기호로 연결하거나, 비교값 위치에 Object {} 또는 Object Array [ {} ] 를 사용하면 Entity 간 Join 을 자동 처리한다.<p/>
        두 개의 FK 를 가진 Associative Table 을 경유하는 Join 처리는 단축 칼럼을 통해 표시할 수 있다. (단축 칼럼은 '+' 기호로 시작한다.)
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {
  /*
    각 캐릭터와 해당 캐릭터가 조종하는 비행선 정보를 표시한다.
    참고로, 'starship' table 은 pilot_id(FK) 를 통해 'character' 테이블과 연결된다.
  */
  // "starship": {},

  /*
   아래는 JEDI 에피소드에 출연한 출연진을 검색하는 예제이다.
   아래 5개의 검색 조건은 동등하나, 출력 결과에 차이가 있다.
  */
  // "characterEpisodeLink": { "episode": { "title": "JEDI" } }
  // "characterEpisodeLink.episode": { "title": "JEDI" }
  // "characterEpisodeLink.episode.title": "JEDI"
  // "+episode": { "title": "JEDI" }
  // "+episode.title": "JEDI"
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
