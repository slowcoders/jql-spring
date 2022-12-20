
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Joined property selection. </H5>
      <div class="details">
        Joined Entity 는 기본적으로 해당 Query Node 검색에 사용된 property 만을 검색 결과에 포함시킨다. <br>
        Query Node 내부에 Property 가 없는 경우엔, 기본으로 Primary Key 만 검색 결과에 포함시킨다. <br>
        명시적으로 해당 Sub Node 의 검색 결과에 포함할 Property 를 지정하고자 할 때에는<br>
        아래의 예와 같이 &lt; &gt; 내부에 Property 명을 나열할 수 있다.<br>
        명시적으로 선택한 Property 외에 QueryNode 내부의 프로퍼티도 포함하고자 하는 경우, 가상 프로퍼티명 '@'를 사용한다.
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const find_friends_of_Han_Solo_having_starship = {
  "name" : "Han Solo",
  "+friend<name>": { "starship<name, @>": { "length@ge": 10 } }
}
const find_friends_of_Luke__1 = {
  "name@Like": "Luke%",
  "+friend<!, name>": {}
}
const find_friends_of_Luke__2 = {
  "+friend<>": { "name": "Luke Skywalker" }
}

const jql = find_friends_of_Han_Solo_having_starship
// const jql = find_friends_of_Luke__1
// const jql = find_friends_of_Luke__2
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
