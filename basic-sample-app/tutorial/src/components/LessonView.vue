<template>
  <form>
    <div class="mb-3">
      <table><row><td>
        <label class="form-label">Table: </label>
      </td><td>
        <b-form-select v-model="selectedTable"
                       :options="tableNames"
                       :disabled="!enable_table_selector"
                       @input="onTableChanged()">
        </b-form-select>
      </td><td>
        <label class="form-label">Sort: </label>
      </td><td>
        <b-form-select
                       class="form-control"
                       v-model="first_sort"
                       @input="onTableChanged()">
          <b-form-select-option :value="''" key="-1">
             First sort
          </b-form-select-option>
          <b-form-select-option
              v-for="(value, i) in columnNames"
              :key="i"
              :value="value.trim()">
            {{ value }}
          </b-form-select-option>
        </b-form-select>
      </td><td>
        <label class="form-label">Limit: </label>
      </td><td>
        <b-form-input v-model="limit"
                       @input="onTableChanged()">
        </b-form-input>
      </td>
      </row>
      </table>
    </div>

    <slot name="description" />

    <!------------>
    <CodeMirror ref="codeView"
                v-model:value="sampleCode"
                :options="editOptions"
                border
                placeholder="test placeholder"
                :height="100 + source_lines * 20"
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

function count_lines(code) {
  const lines = code.split("\n");
  return lines.length;
}


const sampleTables = [
  "character",
  "starship",
  "episode"
]

export default {
  props : {
    js_code : String,
    enable_table_select: Boolean,
  },

  components: { CodeMirror },
  data() {
    return {
      enable_table_selector: this.enable_table_select,
      selectedTable: sampleTables[0],
      tableNames: sampleTables,
      columnNames: ['aaa', 'bbb', 'ccc'],
      first_sort: '',
      columns: '*',
      limit: 3,
      sampleCode: this.make_sample_code(),
      source_lines: count_lines(this.js_code),
      test_result: '',
      sortBy: null,
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
    setTimeout(this.onTableChanged, 10);
  },

  computed: {
  },

  methods : {
    execute() {
      eval(this.sampleCode);
    },

    onTableChanged() {
      console.log("onTableChanged")
      const vm = this;
      vm.sortColumn = null;
      vm.codeView.setValue(vm.make_sample_code());
      vm.resetColumns();
    },

    make_sample_code() {
      const vm = this;

      return ` // JQL Sample
const dbSchema = '${dbSchema}'
const dbTable = '${vm.selectedTable}'
const sort = '${vm.first_sort}'
const limit = ${vm.limit?vm.limit:0}
${vm.js_code}`
    },

    resetColumns() {
      const vm = this;
      axios.get(`http://localhost:6090/api/jql/${dbSchema}/${vm.selectedTable}/metadata/columns`).
      then((res) => {
        const columns = [];
        for (const column of res.data) {
          columns.push(" " + column);
          columns.push("-" + column);
        }
        this.columnNames = columns;
        console.log(res.data);
      })
    },

    onFirstSortChanged(column) {
      this.onTableChanged();
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
