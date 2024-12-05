<template>
  <form>
    <div>
      <div style="background-color: #F0F0F0">
        <table>
          <tbody>
            <tr>
              <td>
                <label class="form-label">Table: </label>
              </td><td class="input-column">
                <b-form-select v-model="selectedTable"
                                :options="sampleTables"
                                @input="onTableChanged()">
                </b-form-select>
              </td><td width="50%">
              </td><td class="input-column">
                <b-button variant="success" @click="save()">
                  Sync
                </b-button>
              </td>
            </tr>
          </tbody>  
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
import "choices.js/public/assets/styles/choices.css";
import "@formio/js/dist/formio.full.css"
import "@/css/header.css"

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
  "customer",
  "publisher",
  "book_order",
]

export default {
  data() {
    return {
      showSchemaInfo: false,
      storageNames: sampleStorages,
      sampleTables: sampleTables,
      selectedStorage: sampleStorages[0],
      selectedTable: sampleTables[0],
      schemaInfo: '',
      hqlRepo: null,
      columns: [],
    }
  },

  mounted() {
    setTimeout(this.onTableChanged, 10);
  },

  methods : {
    async onTableChanged() {
      const vm = this;
      const columns = formSchema[vm.selectedTable];
      await vm.setFormModel(columns);
      const model = {
        components: [ {
          label: vm.selectedTable,
          key: "children",
          type: "editgrid",
          input: true,
          components: columns, 
          // templates: {
          //   row: `<div class=row> 
          //     {%util.eachComponent(components, function(component) { %}
          //       <div class="col-sm-2">
          //         {{ row[component.key] }}
          //       </div>
          //     {% }) %}
          //     </div>`
          // }
        }]
      }
      const select = columns.map(row => row.key)
      vm.hqlRepo = new HqlApi(`${baseUrl}/bookstore/${vm.selectedTable}`);
      const res = await vm.hqlRepo.find(null, {select});
      Formio.createForm(document.getElementById('formio'), model)
      .then((form) => {
        vm.form = form;
        form.submission = {
          data: {
            children: JSON.parse(JSON.stringify(res.content))
          }
        }
        form.on('submit', function(submission) {
          for (const row of submission.data.children) {
            const org = res.content.find((r) => r.id === row.id);
            if (!org) {
              vm.hqlRepo.insert(row, 'ignore');
            } else if (JSON.stringify(row) !== JSON.stringify(org)) {
              vm.hqlRepo.updateByIdList([row.id], row);
            }
          }
          for (const row of res.content) {
            const org = submission.data.children.find((r) => r.id === row.id);
            if (!org) {
              vm.hqlRepo.delete(row.id);
            }
          }
          return true;          
        });
      })
    },

    save() {
      this.form.submit();
    },

    async setFormModel(columns) {
      this.columns = columns;
      for (const col of columns) {
        const ref = col.dataRef;
        if (ref) {
          const api = new HqlApi(`${baseUrl}/bookstore/${ref.schema}`);
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
  HqlForm.number('id', "아이디"),
  HqlForm.text('title', "제목"),
  HqlForm.select('author_id', "저자", {
      schema: 'author',
      value: 'id',
      label: 'name'
  }),
  HqlForm.select('publisher_id', "출판사", {
      schema: 'publisher',
      value: 'id',
      label: 'name'
  }),
  HqlForm.number('price', "가격"),
]

let author_columns = [
    HqlForm.number('id', "아이디"),
    HqlForm.text('name', "이름"),
    // HqlForm.text('profile.hometown', "출생지"),
    // HqlForm.text('profile.country', "국적"),
    // HqlForm.number('profile.birthYear', "출생년도"),
]

let customer_columns = [
    HqlForm.number('id', "아이디"),
    HqlForm.text('name', "이름"),
    HqlForm.tags('memo.favoriteGenre', '선호 장르', ['추리', '스릴러', 'SF', '로맨스', '무협', '공포', '판타지']),
]

let publisher_columns = [
    HqlForm.number('id', "아이디"),
    HqlForm.text('name', "이름"),
]

let order_columns = [
  HqlForm.select('customer_id', "고객", {
      schema: 'customer',
      value: 'id',
      label: 'name'
  }),
  HqlForm.select('book_id', "책", {
      schema: 'book',
      value: 'id',
      label: 'title'
  }),
  // HqlForm.number('book.price', '가격'),
  // HqlForm.text('book.publisher.name', '출판사')
]

const formSchema = {
  book: book_columns,
  author: author_columns,
  book_order: order_columns,
  customer: customer_columns,
  publisher: publisher_columns,
};
</script>
