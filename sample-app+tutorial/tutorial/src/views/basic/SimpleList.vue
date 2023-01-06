<template>
  <LessonView
      :js_code="code"
      :enable_table_select="true">
    <template v-slot:description>
      <H5> 전체 검색 </H5>
      <div class="details">
        전체 Entity List 에 대한 단순 검색 예.<br>
        검색할 Table, Sort, 검색 결과에 포함할 칼럼과 최대 개수를 지정할 수 있다.<br>
      </div>
    </template>
  </LessonView>
</template>

<script>
import LessonView from "@/components/LessonView";

const sample_code = `
const jql = {}
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
