<template>
  <form>
    <div class="mb-3">
      <label class="form-label">Email address</label>
      <Codemirror id="code-editor"
                  ref="cm"
          v-model:value="code"
          :options="cmOptions"
          border
          placeholder="test placeholder"
          :height="200"
          :aria-readonly="true"
          @change="change"
      />
    </div>
    <b-button @click="execute">
      run
    </b-button>
    <div class="mb-3">
      <label for="exampleFormControlTextarea1" class="form-label">Example textarea</label>
      <Codemirror id="result-view"
                  v-model:value="test_result"
                  :options="cmOptions"
                  border
                  placeholder="test placeholder"
                  :height="200"
                  :aria-readonly="true"
                  @change="change"
      />
    </div>
  </form>
</template>

<script>
import Codemirror from "codemirror-editor-vue3";

// language
import "codemirror/mode/javascript/javascript.js";

// theme
import "codemirror/theme/dracula.css";

import { ref } from "vue";

import axios from "axios";

export default {
  props : {
    js_code : String
  },

  components: { Codemirror },
  setup(props) {
    // const code = ref(this.js_code)
ref(`
var i = 0;
for (; i < 9; i++) {
  console.log(i);
  // more statements
}`);

    return {
      code: props.js_code ,
      test_result: '',
      axios: axios,
      cmOptions: {
        mode: "text/javascript", // Language mode
        theme: "dracula", // Theme
        lineNumbers: true, // Show line number
        smartIndent: true, // Smart indent
        indentUnit: 4, // The smart indent unit is 2 spaces in length
        foldGutter: true, // Code folding
        styleActiveLine: true, // Display the style of the selected row
        readOnly: true
      },
    };
  },
  data() {
    return {
      http_res: ''
    }
  },
  methods : {
    execute() {
      const vm = this;
      this.exampleFormControlTextarea1 = vm.code;
      eval(vm.code);
      vm.http_res.then((res) => {
        vm.$refs.cm.setValue = (res.data);
        vm.test_result = res.data;
        console.log(res.data)
      })
    }
  }
};

</script>

<style>
  form {
    padding-top: 2em;
    padding-right: 2em;
  }
  .CodeMirror * {
    font-family: Curier, monospace;
    font-size: small;
  }

</style>
