<template>
  <LI> 전체 검색 </LI>
  <About
      :js_code="code"
  />
</template>

<script>
import About from "./About";

const sample_code = `
const dbSchema = 'petclinic'
const dbTable = 'pets'
const jql = {}
this.http_res = this.axios.post(\`http://localhost:6090/api/jql/\${dbSchema}/\${dbTable}/find\`, jql)
`
export default {
  components: {
    About
  },

  data() {
    return {
      code: sample_code
    }
  }
}
</script>
