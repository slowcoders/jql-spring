<template>
  <form>
    <div class="mb-3">
      <label class="form-label">Email address</label>
      <CodeMirror
          v-model:value="code"
          :options="editOptions"
          border
          placeholder="test placeholder"
          :height="200"
          @change="change"
      />
    </div>
    <b-button @click="execute">
      run
    </b-button>
    <div class="mb-3">
      <label class="form-label">Example textarea</label>
      <CodeMirror
          class="test-result-view" ref="resultView"
          v-model:value="test_result"
          :options="viewOptions"
          border
          placeholder="test placeholder"
          :height="600"
          :aria-readonly="true"
          @change="change"
      />
    </div>
  </form>
</template>

<script>
import CodeMirror from "codemirror-editor-vue3";
import "codemirror/mode/javascript/javascript.js";
import "codemirror/theme/dracula.css";

import { ref } from "vue";

import axios from "axios";

export default {
  props : {
    js_code : String
  },

  components: { CodeMirror },
  setup(props) {
    return {
      code: props.js_code,
      test_result: '',
      axios: axios,
      editOptions: {
        mode: "text/javascript", // Language mode
        theme: "default", // Theme
        lineNumbers: true, // Show line number
        smartIndent: true, // Smart indent
        indentUnit: 4, // The smart indent unit is 2 spaces in length
        foldGutter: true, // Code folding
        styleActiveLine: true, // Display the style of the selected row
      },
      viewOptions: {
        mode: "text/javascript", // Language mode
        theme: "dracula", // Theme
        lineNumbers: false, // Show line number
        smartIndent: true, // Smart indent
        indentUnit: 4, // The smart indent unit is 2 spaces in length
        foldGutter: true, // Code folding
        styleActiveLine: true, // Display the style of the selected row
      },
    };
  },
  data() {
    return {
      http_res: ''
    }
  },
  mounted() {
    this.resultView = this.$refs.resultView.cminstance;
  },
  methods : {
    execute() {
      eval(this.code);
    },

    http_post(url, jql) {
      const vm = this;
      axios.post(url, jql).then((res) => {
        vm.resultView.setValue(JSON.stringify(res.data, null, 4));
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
  .test-result-view .CodeMirror-cursor {
    display: none !important
  }

</style>
