<template>
  <form>
    <div class="mb-3">
      <table><row><td>
        <label class="form-label">Table: </label>
      </td><td>
        <b-form-select v-model="selectedTable"
                       :options="tableNames"
                       @input="onTableChanged()">
        </b-form-select>
      </td><td>
        <b-form-select
                       class="form-control"
                       v-model="first_sort"
                       @input="onFirstSortChanged()">
          <b-form-select-option :value="null" key="-1">
             First sort
          </b-form-select-option>
          <b-form-select-option
              v-for="(option, i) in columnNames"
              :key="i"
              :value="option">
            {{ option }}
          </b-form-select-option>
        </b-form-select>
      </td>
      </row>
      </table>
    </div>

    <!------------>
    <CodeMirror ref="codeView"
                v-model:value="sampleCode"
                :options="editOptions"
                border
                placeholder="test placeholder"
                :height="200"
    />

    <b-button @click="execute">
      run
    </b-button>
    <br>
    <CodeMirror ref="resultView"
        class="test-result-view"
        v-model:value="test_result"
        :options="viewOptions"
        border
        placeholder="test placeholder"
        :height="600"
        :aria-readonly="true"
    />
  </form>
</template>

<script>
import CodeMirror from "codemirror-editor-vue3";
import "codemirror/mode/javascript/javascript.js";
import "codemirror/theme/dracula.css";

import { ref } from "vue";

import axios from "axios";

const dbSchema = 'starwars';
function make_sample_code(table, js_code) {
  return ` // JQL Sample
const dbSchema = '${dbSchema}'
const dbTable = '${table}'
${js_code}`
}

const sampleTables = [
  "character",
  "starship",
  "episode"
]

export default {
  props : {
    js_code : String
  },

  components: { CodeMirror },
  data() {
    return {
      selectedTable: sampleTables[0],
      tableNames: sampleTables,
      columnNames: ['aaa', 'bbb', 'ccc'],
      first_sort: null,
      sampleCode: make_sample_code('character', this.js_code),
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
      }
    }
  },

  mounted() {
    this.codeView = this.$refs.codeView.cminstance;
    this.resultView = this.$refs.resultView.cminstance;
  },

  computed: {
  },

  methods : {
    execute() {
      eval(this.sampleCode);
    },

    onTableChanged() {
      const vm = this;
      vm.sortColumn = null;
      vm.codeView.setValue(make_sample_code(vm.selectedTable, vm.js_code));
      vm.resetColumns();
    },

    resetColumns() {
      const vm = this;
      axios.get(`http://localhost:6090/api/jql/${dbSchema}/${vm.selectedTable}/metadata/columns`).
      then((res) => {
        vm.columnNames = res.data;
        console.log(res.data);
      })
    },

    onFirstSortChanged() {
    },

    http_post(url, jql) {
      const vm = this;
      axios.post(url, jql).
      then((res) => {
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
  table td {
    padding: 5px
  }

</style>
