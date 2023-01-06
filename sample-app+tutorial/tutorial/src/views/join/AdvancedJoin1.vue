
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Property selection. </H5>
      <div class="details">
        <b>Aliases for output property selection.</b><br>
          <b class="alias"> @ </b> : Auto. (<i>default</i>) <br>
          <b class="alias"> 0 </b> : Primary Keys<br>
          <b class="alias"> * </b> : All properties<br>
        <br>
        Joined Entity 의 기본 select 값은 Auto('@') 이다. Query Node 내부에 비교 조건이 사용된 경우엔 해당 Property 들을 출력에 포함시키고, 비교 조건이 없는 경우엔 Primary Key 를 선택한다. <br>
        명시적으로 해당 Sub Node 의 검색 결과에 포함할 Property 를 지정하고자 할 때에는 아래의 예와 같이 &lt; &gt; 내부에 Property 명을 나열한다. <br>
        Alias 와 Property 명을 혼합하여 사용할 수 있다.
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
  "+friend<0, name>": {}
}
const find_friends_of_Luke__without_sub_entities = {
  "+friend<>": { "name": "Luke Skywalker" }
}

const jql_1 = find_friends_of_Han_Solo_having_starship
const jql_2 = find_friends_of_Luke__1
const jql_3 = find_friends_of_Luke__without_sub_entities

/** 아래의 대입문을 수정하여 jql_1/2/3 의 검색 결과를 비교해 본다 */
const jql = jql_1;
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

<style>
b.alias {
  display: inline-block;
  font-weight: bold;
  width: 2ex;
  text-align: center;
  margin-left: 2ex;
}
</style>