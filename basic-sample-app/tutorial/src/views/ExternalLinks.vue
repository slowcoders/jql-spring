<template>
  <div class="hello">
    <br>
    <h1>JQL Sample & Tutorial</h1>
    <h3>Essential Links</h3>
    <ul>
      <li><a href="/#/basic/list" target="_blank" rel="noopener">Tutorial{{typeof new Array}}</a></li>
      <li><a href="http://localhost:6090/swagger-ui.html" target="_blank" rel="noopener">API Swagger</a></li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'ExternalLinks',
  props: {
    msg: String
  },
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
li {
  margin: .5em
}
a {
  color: #42b983;
}
</style>
