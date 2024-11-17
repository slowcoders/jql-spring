<template>
  <form>
    <div>
      <div style="background-color: #F0F0F0">
        <table>
          <tr>
            <td>
              <label class="form-label">Table: </label>
            </td><td class="input-column">
              <b-form-select v-model="selectedTable"
                              :options="sampleTables"
                              @input="onTableChanged()">
              </b-form-select>
            </td>
          </tr>
        </table>
      </div>
      <br>
      <slot name="description" />

    </div>

    <!------------>
      <div id="formio" />
  </form>
</template>

<script>
// import CodeMirror from "codemirror-editor-vue3";
// import "codemirror/mode/javascript/javascript.js";
// import "codemirror/theme/dracula.css";
import "choices.js/public/assets/styles/choices.css";
import "@formio/js/dist/formio.full.css"

import { ref } from "vue";

import axios from "axios";
import { HqlApi, HqlForm } from "@/api/hqlApi";
import { Formio } from "@formio/js";

const dbSchema = 'bookstore';
const baseUrl = 'http://localhost:7007/api/hql'

const sampleStorages = [
  "bookstore",
  "bookstore_jpa",
]

const sampleTables = [
  "author",
  "book",
  "book_order",
]

export default {

  // components: { CodeMirror },
  data() {
    return {
      showSchemaInfo: false,
      storageNames: sampleStorages,
      sampleTables: sampleTables,
      selectedStorage: sampleStorages[0],
      selectedTable: sampleTables[0],
      schemaInfo: '',
      columns: [],
    }
  },

  mounted() {
    // this.codeView = this.$refs.codeView.cminstance;
    setTimeout(this.onTableChanged, 10);
  },

  computed: {
  },

  methods : {

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

    onUpdateSchema() {
      alert("onUpdateSchema");
    },

    onAddEntity() {
      const model = {components: [...this.columns, {
            type: 'button',
            action: 'submit',
            label: 'Submit',
            theme: 'primary'
          }
        ]
      }
      Formio.createForm(document.getElementById('formio'), model)
      .then(function(form) {
        // form.submission = {
        //   data: {
        //     title: '그리스인 조르바',
        //     price: 10000,
        //   }
        // }
        form.on('submit', function(submission) {
          alert(JSON.stringify(submission.data, null, 2));
          console.log(submission);
        });
      });
    },

    async onTableChanged() {
      const vm = this;
      const columns = formSchema[vm.selectedTable];
      await vm.setFormModel(columns);
      const model = {
        components: [ {
          type: "editgrid",
          label: vm.selectedTable,
          key: "children",
          input: false,
          components: columns, 
        }]
      }
      const select = columns.map(row => row.key)
      const api = new HqlApi(`${baseUrl}/bookstore/${vm.selectedTable}`);
      const res = await api.find(null, {select});
      console.log("datagrid", res)
      Formio.createForm(document.getElementById('formio'), model)
      .then(function(form) {
        form.submission = {
          data: {
            children: res.content
          }
        }
        form.on('submit', function(submission) {
          alert(JSON.stringify(submission.data, null, 2));
          console.log(submission);
        });
      });



      if (vm.showSchemaInfo) {
        const url = `${baseUrl}/metadata/${dbSchema}/${vm.selectedTable}/FormModel`
        axios.get(url).then(res => {
          vm.schemaInfo = `\n/*************** Schema<${vm.selectedTable}> ***********************\n${res.data}*/`;
          console.log("schemaInfo", vm.schemaInfo)
        }).catch(vm.show_http_error);

        vm.codeView.setValue(vm.make_sample_code());
      }
    },

    async setFormModel(columns) {
      this.columns = columns;
      for (const col of columns) {
        const ref = col.dataRef;
        if (ref) {
          const api = new HqlApi(`${baseUrl}/bookstore/${ref.table}`);
          const select = ref.value + ',' + ref.label
          const data = await api.find(ref.filter, {select});
          col.data = {
            values: data.content.map(item => ({
              value: item[ref.value],
              label: item[ref.label],
            }))}
          ;
          console.log(col.key, col.data)
        }
      }
    },

    show_http_error(err) {
      alert(err.message + "\n" + JSON.stringify(err.response, null, 4));
    },
  }
};

let book_columns = [
  HqlForm.hidden('id'),
  HqlForm.text('title', "제목"),
  HqlForm.select('author.id', "저자", {
      table: 'author',
      value: 'id',
      label: 'name'
  }),
  HqlForm.text('price', "가격"),
]

let autor_columns = [
    HqlForm.number('id', "아이디"),
    HqlForm.text('name', "이름"),
    HqlForm.text('profile', "소개"),
]

let customer_columns = [
    HqlForm.number('id', "아이디"),
    HqlForm.text('name', "이름"),
    HqlForm.text('metadata', "소개"),
]


let order_columns = [
  HqlForm.select('customer.id', "고객", {
      table: 'customer',
      value: 'id',
      label: 'name'
  }),
  HqlForm.select('book.id', "책", {
      table: 'book',
      value: 'id',
      label: 'title'
  }),
  // HqlForm.date('date', '주문일')
]

const formSchema = {
  book: book_columns,
  author: autor_columns,
  book_order: order_columns,
  customer: customer_columns
};


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
