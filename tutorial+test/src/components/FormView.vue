<template>
  <form>
    <div>
      <div style="background-color: #F0F0F0">
        <table><tr><td>
          <label class="form-label">Storage: </label>
        </td><td class="input-column">
         <label class="form-label">Table: </label>
       </td><td class="input-column">
         <b-form-select v-model="selectedTable"
                        :options="tableNames"
                        :disabled="!showSchemaInfo"
                        @input="onTableChanged()">
         </b-form-select>
       </td><td>
        </td><td width="30%">
        </td><td>
          <b-form-button
                         class="form-control"
                         @click="onUpdateSchema()">
              Update Form Schema
          </b-form-button>
        </td><td>
          <b-form-button
                         class="form-control"
                         @click="onAddEntity()">
              Add Entity
          </b-form-button>
        </td>
        </tr>
        </table>
      </div>
      <br>
      <slot name="description" />

    </div>

    <!------------>
    <div id="code-area">
      <div class="code" style="position: relative">
        <CodeMirror ref="codeView"
                    v-model:value="sampleCode"
                    :options="editOptions"
                    border
                    placeholder="test placeholder"
        />

        <b-button style="position: absolute; top:5px; right: 10px" @click="execute">
          run
        </b-button>
      </div>
      <div class="code">
        <CodeMirror ref="resultView"
            class="test-result-view col-sm-6"
            v-model:value="test_result"
            :options="viewOptions"
            border
            placeholder="test placeholder"
            :aria-readonly="true"
        />
      </div>
    </div>
  </form>
</template>

<script>
import CodeMirror from "codemirror-editor-vue3";
// import "https://cdn.form.io/js/formio.embed.js"
import "codemirror/mode/javascript/javascript.js";
import "codemirror/theme/dracula.css";

import { ref } from "vue";

import axios from "axios";
import { Formio } from "@formio/js";

const dbSchema = 'bookstore';
const baseUrl = 'http://localhost:7007/api/hql'

function count_lines(code) {
  const lines = code.split("\n");
  return lines.length;
}

const sampleStorages = [
  "bookstore",
  "bookstore_jpa",
]

const sampleTables = [
  "customer",
  "book",
  "episode",
]

export default {
  props : {
    js_code : String,
  },

  components: { CodeMirror },
  data() {
    return {
      showSchemaInfo: true,
      storageNames: sampleStorages,
      selectedStorage: sampleStorages[0],
      tableNames: sampleTables,
      selectedTable: sampleTables[0],
      schemaInfo: '',
      selectableColumns: [],
      selectedColumns: [],
      allColumnSelected: true,
      sortOptions: [],
      first_sort: '',
      columns: '*',
      limit: 0,
      sampleCode: "--", //this.make_sample_code(),
      source_lines: count_lines(this.js_code),
      test_result: '',
      sortBy: null,
      cntTest: 0,
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
        indentUnit: 2, // The smart indent unit is 2 spaces in length
        foldGutter: true, // Code folding
        lineWrapping: true,
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
      try {
        eval(this.sampleCode);
      } catch (e) {
        this.show_error_in_result_view("Test source compile error.\n" + e.message);
      }
    },


    to_url_param(options) {
      if (!options) return "";

      let params = ""
      for (const k in options) {
        params += params.length > 1 ? '&' : '?';
        params += k + "=" + options[k];
      }
      return params;
    },

    make_http_param() {
      const vm = this;
      let param = "select=";
      param += (vm.selectedColumns?.length > 0) ? vm.selectedColumns : '${hql_select}'
      if (vm.first_sort?.length > 0) {
        param += '&sort=' + vm.first_sort
      }
      if (vm.limit > 0) {
        param += '&limit=' + vm.limit
      }
      return param;
    },

    make_sample_code() {
      const vm = this;

      return `${vm.schemaInfo}`
    },

    show_error_in_result_view(msg) {
      this.resultView.setValue("!!!! " + msg);
    },

    onUpdateSchema() {
      alert("onUpdateSchema");
    },

    onAddEntity() {
      Formio.createForm(document.getElementById('formio'), form_schema).then(function(form) {
        form.on('submit', function(submission) {
          alert(JSON.stringify(submission.data, null, 2));
          console.log(submission);
        });
      });
    },

    onTableChanged() {
      const vm = this;
      if (vm.showSchemaInfo) {
        const url = `${baseUrl}/metadata/${dbSchema}/${vm.selectedTable}/FormModel`
        axios.get(url).then(res => {
          vm.schemaInfo = `\n/*************** Schema<${vm.selectedTable}> ***********************\n${res.data}*/`;
          console.log("schemaInfo", vm.schemaInfo)
        }).catch(vm.show_http_error)
        vm.codeView.setValue(vm.make_sample_code());
      }
    },

    show_http_error(err) {
      let msg = err.message + "\n" + JSON.stringify(err.response, null, 4);
      this.show_error_in_result_view(msg);
    },

    http_post(url, hql) {
      const vm = this;
      const options = {
        headers: { "Content-Type": `application/json`}
      }
      axios.post(url, hql, options).then(res => {
        vm.cntTest ++;
        const header = "ex " + vm.cntTest + ") result: " + res.data.content.length + "\n\n";
        const results = JSON.stringify(res.data.content, null, 2);
        const sql = res.data.metadata?.lastExecutedSql ? "\n\n---------------\nexecuted sql:\n" + res.data.metadata.lastExecutedSql : "";
        vm.resultView.setValue(header + results + sql);
      }).catch(vm.show_http_error)
    }
  }
};


let form_schema = {
  components: [
    {
      type: 'textfield',
      key: 'firstName',
      label: 'First Name',
      placeholder: 'Enter your first name.',
      input: true,
      tooltip: 'Enter your <strong>First Name</strong>',
      description: 'Enter your <strong>First Name</strong>'
    },
    {
      type: 'textfield',
      key: 'lastName',
      label: 'Last Name',
      placeholder: 'Enter your last name',
      input: true,
      tooltip: 'Enter your <strong>Last Name</strong>',
      description: 'Enter your <strong>Last Name</strong>'
    },
    {
      type: "select",
      label: "Favorite Things",
      key: "favoriteThings",
      placeholder: "These are a few of your favorite things...",
      data: {
        values: [
          {
            value: "raindropsOnRoses",
            label: "Raindrops on roses"
          },
          {
            value: "whiskersOnKittens",
            label: "Whiskers on Kittens"
          },
          {
            value: "brightCopperKettles",
            label: "Bright Copper Kettles"
          },
          {
            value: "warmWoolenMittens",
            label: "Warm Woolen Mittens"
          }
        ]
      },
      dataSrc: "values",
      template: "<span>{{ item.label }}</span>",
      multiple: true,
      input: true
    },
    {
      type: 'button',
      action: 'submit',
      label: 'Submit',
      theme: 'primary'
    }
  ]
}

</script>

<style>
  form {
    padding-top: 2em;
    padding-right: 2em;
    padding-bottom: 1em;
    display: grid;
    grid-template-rows: auto 1fr;
    height: 100vh;
    max-height: 100vh;
  }

  /*.test-result-view .CodeMirror {*/
  /*  overflow: auto;*/
  /*  height: 100%;*/
  /*}*/

  td > label {
    padding-top: 0.5em;
  }

  td.input-column {
    padding-right: 2em;
  }

  .details {
    padding-left: 1em;
    margin-bottom: 0.7em;
  }

  #code-area {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
  div.code {
    /*overflow: auto;*/
    /*max-height: 100%;*/
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
