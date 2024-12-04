import {describe, expect, test} from '@jest/globals';
import axios from "axios";
import fs from "fs"

describe('JPA Source Generation Test', () => {
  test.each([
    { table: "Customer" },
    { table: "Author" },
    { table: "Book" },
  ]) ('소스 비교 테스트', async ({table}) => {
    if (false) {
      const ref_file = `../sample-app/src/main/java/org/slowcoders/hyperql/sample/jpa/bookstore_jpa/model/${table}.java`;
      let src = fs.readFileSync(ref_file, {encoding:'utf8', flag:'r'});
      src = src.substring(src.indexOf('@'));
      src = src.replace(/\s/g, '');

      const req_url = `http://localhost:7007/api/hql/metadata/bookstore/${table}/SpringJPA`
      let res = await axios.get(req_url);
      res = res.data;
      res = res.substring(res.indexOf('@'));
      res = res.replaceAll('"bookstore"', '"bookstore_jpa"');
      res = res.replace(/\s/g, '');
      expect(res).toBe(src);
    }
  });
});

