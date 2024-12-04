
<template>
  <LessonView
      :js_code="code"
      target_table="customer">
    <template v-slot:description>
      <H5> 검색 조건의 Or 결합 </H5>
      <div class="details">
      * HQL 은 기본적으로 {} 내부의 조건절을 And 조건으로, [] 내부의 조건절은 Or 조건으로 결합한다.
        {} 내부의 조건절을 Or 조건으로 결합하려면 'OR': { 조건절, ... } 형식을 사용한다.<br>
        다수의 Or 조건절을 And 조건으로 결합하려면 'AND': [ {Or 조건절}, ... ] 형태를 이용한다.<br>
        참고로, 동일한 프로퍼티를 비교하는 경우, 다중값 어레이 또는 @between 을 사용하여 간명하게 표현하는 것이 보다 바람직하다.</div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const hql_select = AUTO;
const min_height = 1.2
const max_height = 2.0
const min_weight = 50
const max_weight = 120

const normal_height = { "height >=": min_height, "height <=": max_height }
const normal_mass   = { "mass >=":   min_weight, "mass <=":   max_weight }

const too_small_or_too_tall  = { OR: { "height <": min_height, "height >": max_height }}
const too_light_or_too_heavy = { OR: { "mass <":   min_weight, "mass >":   max_weight }}
const too_small_or_too_heavy = { OR: { "height <": min_height, "mass >":   max_weight }}


const too_small_or_too_tall__AND__too_light_or_too_heavy = { 
  AND: [ too_small_or_too_tall, too_light_or_too_heavy ]
}

const too_small_or_too_tall__AND__too_light_or_too_heavy__short_expression = {
    "height !between": [min_height, max_height],
    "mass !between": [min_weight, max_weight],
}

/* 아래의 주석을 한 줄씩 번갈아 해제하면서 검색 결과의 차이를 비교해 보십시오. */
const hql_filter = too_small_or_too_tall;
// const hql_filter = too_light_or_too_heavy;
// const hql_filter = too_small_or_too_heavy;
// const hql_filter = too_small_or_too_tall__AND__too_light_or_too_heavy;
// const hql_filter = too_small_or_too_tall__AND__too_light_or_too_heavy__short_expression;

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
