import {beforeAll, describe, expect, test} from '@jest/globals';
import { hqlApi } from '@/sample_db'

describe('Comapre', () => {
  let refs;

  beforeAll(async () => {
    const filter = {}
    const res = await hqlApi.find(filter, {limit: 2, sort: "id" });
    refs = res.content;
    expect(refs.length).toBe(2);
  })

  test('@is: [] (default)', async () => {
    const filter = {
      "id": [refs[0].id, refs[1].id]
    }
    const res = await hqlApi.find(filter, { sort: "id" });
    const authors = res.content;
    expect(authors.length).toBe(refs.length);
    expect(authors[0].id).toBe(refs[0].id);
    expect(authors[1].id).toBe(refs[1].id);
  });

  test('@is: [] (explicit)', async () => {
    const filter = {
      "id": [refs[0].id, refs[1].id]
    }
    const res = await hqlApi.find(filter, { sort: "id" });
    const authors = res.content;
    expect(authors.length).toBe(refs.length);
    expect(authors[0].id).toBe(refs[0].id);
    expect(authors[1].id).toBe(refs[1].id);
  });

  test('@not: []', async () => {
    const filter = {
      "id@not": [refs[0].id, refs[1].id]
    }
    const count = await hqlApi.count();
    const res = await hqlApi.find(filter);
    const authors = res.content;
    expect(authors.length).toBe(count - refs.length)
    const id_set = {};
    for (const author of authors) {
      id_set[author.id] = 0;
    }
    expect(id_set[refs[0].id]).toBeUndefined();
    expect(id_set[refs[1].id]).toBeUndefined();
  });

  test('@like []', async () => {
    const filter = {
      "name@like": [refs[0].name.substring(0, 4) + "%", refs[1].name.substring(0, 4) + "%"] 
    }
    const res = await hqlApi.find(filter);
    const authors = res.content;
    expect(authors.length).toBeGreaterThanOrEqual(refs.length)
    const id_set = {};
    for (const author of authors) {
      id_set[author.id] = 0;
    }
    expect(id_set[refs[0].id]).not.toBeUndefined();
    expect(id_set[refs[1].id]).not.toBeUndefined();
  });

  test('@not like []', async () => {
    const filter = {
      "name@not like": [refs[0].name.substring(0, 4) + "%", refs[1].name.substring(0, 4) + "%"] 
    }
    const res = await hqlApi.find(filter);
    const authors = res.content;
    expect(authors.length).toBeGreaterThanOrEqual(refs.length)
    const id_set = {};
    for (const author of authors) {
      id_set[author.id] = 0;
    }
    expect(id_set[refs[0].id]).toBeUndefined();
    expect(id_set[refs[1].id]).toBeUndefined();
  });
});

