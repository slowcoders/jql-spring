<template>
  <LessonView
      :js_code="code"
      :enable_table_select="true">
    <template v-slot:description>
      <H5> 전체 검색 </H5>
      <div class="details">
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
