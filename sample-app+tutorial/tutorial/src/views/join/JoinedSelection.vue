
<template>
  <LessonView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <H5> Property selection of Joined entity. </H5>
      <div class="details">
        <b>Aliases for property selection.</b><br>
          <b class="alias"> * </b> : All properties<br>
          <b class="alias"> @ </b> : Auto. <br>
          <b class="alias"> 0 </b> : Primary Keys<br>
        Join 된 엔터티의 일부 Property 만을 검색하고자 하는 경우, &lt; &gt; 내부에 포함할 Property 를 나열한다. &lt;0, name&gt; 과 같이 Alias 와 Property 명을 혼합하여 사용할 수 있다.<br>
        Property selection 이 지정되지 않은 경우, 상위 Node 에 지정된 Selection 목록 중 alias 들을 추출하여 기본값으로 사용한다.<br>
        (상위 Query Node 의 Selection 문에 Alias 가 포함되지 않은 경우, Nothing(&lt;&gt;) 을 기본값으로 취한다.)<br>
        <br>
        Auto('@') 를 지정하면, 비교문에 사용된 property 들을 자동으로 선택하되, 고정된 값을 가지는 Property 는 제외한다.
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const find_friends_of_Han_Solo_having_starship = {
  "name" : "Han Solo",
  "+friend<name>": { "starship<name>": {} }
}
const find_friends_of_Luke = {
  "name@Like": "Luke%",
  "+friend<0, name>": {}
}
const find_friends_of_Luke__without_sub_entities = {
  "+friend<>": { "name": "Luke Skywalker" }
}
const find_friends_of_Luke__without_fixed_sub_entities = {
  "+friend<@>": { "name": "Luke Skywalker" }
}
const find_pilot_having_big_starships = {
  "starship": { "length@ge": 10 }
}
const find_pilot_having_big_starships__with_auto_sub_entity_selection = {
  "starship<@>": { "length@ge": 10 }
}
const find_pilot_having_big_starships__without_sub_entity_selection = {
  "starship.length@ge": 10
}


const jql_1 = find_friends_of_Han_Solo_having_starship
const jql_2 = find_friends_of_Luke
const jql_3 = find_friends_of_Luke__without_sub_entities
const jql_4 = find_friends_of_Luke__without_fixed_sub_entities
const jql_5 = find_pilot_having_big_starships
const jql_6 = find_pilot_having_big_starships__with_auto_sub_entity_selection
const jql_7 = find_pilot_having_big_starships__without_sub_entity_selection

/** 아래의 대입문을 수정하여 jql_1~7 의 검색 결과를 비교해 본다 */
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
  width: 5ex !important;
  text-align: center;
  margin-left: 2ex;
}
</style>