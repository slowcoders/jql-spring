
<template>
  <FormView
      :js_code="code"
      :enable_table_select="false">
    <template v-slot:description>
      <div class="details">
        <h5> Simple Form: form.io 연동 데모 </h5>
      </div>
      <div id="formio" />
    </template>
  </FormView>
</template>

<script>
import FormView from "@/components/FormView";

const sample_code = `
/* 아래의 주석을 한 줄씩 해제하면서 검색 결과의 차이를 비교해 보십시오. */
const hql_select = "0"
  //             + ", *"
  //             + ", book_.0"
  //             + ", episode_"
  //             + ", friend_.name"
  //             + ", friend_(name, friend_.name)"

const hql_filter = {}
`

export default {
  components: {
    FormView,
  },

  data() {
    return {
      code: sample_code
    }
  }
}

</script>
