
<template>
  <LessonView
      :js_code="code"
      target_table="customer">
    <template v-slot:description>
      <H5> Json column search. </H5>
      <div class="details">
        Json 객체를 저장한 Column 은 Joined Entity 와 동일한 문법으로 검색한다.
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const hql_select = AUTO;

const hql_1 = {
  "memo" : { "favoriteFood" : "kimchi" }
}
const hql_2 = {
  "memo" : { "shoeSize@ge" : 270 }
}

/* 아래의 주석을 한 줄씩 번갈아 해제하면서 검색 결과의 차이를 비교해 보십시오. */
const hql_filter = hql_1;
// const hql_filter = hql_2;
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
