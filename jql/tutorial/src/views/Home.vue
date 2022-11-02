<template>
  <About
      :js_code="code"
  />
</template>

<script>
import About from "./About";

export default {
  components: {
    About
  },

  data() {
    return {
      code: `
var i = 0;
for (; i < 9; i++) {
  console.log(i);
  // more statements
}`
    }
  }
}
</script>
