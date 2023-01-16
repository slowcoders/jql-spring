import {beforeAll, describe, expect, test} from '@jest/globals';
import { jqlApi } from '@/api/jqlApi'

describe('Comapre', () => {
  let refs;

  beforeAll(async () => {
    const jql = {}
    const res = await jqlApi.find(jql, {limit: 2, sort: "id" });
    refs = res.content;
    expect(refs.length).toBe(2);
  })

  test('@is: [] (default)', async () => {
    const jql = {
      "id": [refs[0].id, refs[1].id]
    }
    const res = await jqlApi.find(jql, { sort: "id" });
    const characters = res.content;
    expect(characters.length).toBe(refs.length);
    expect(characters[0].id).toBe(refs[0].id);
    expect(characters[1].id).toBe(refs[1].id);
  });

  test('@is: [] (explicit)', async () => {
    const jql = {
      "id": [refs[0].id, refs[1].id]
    }
    const res = await jqlApi.find(jql, { sort: "id" });
    const characters = res.content;
    expect(characters.length).toBe(refs.length);
    expect(characters[0].id).toBe(refs[0].id);
    expect(characters[1].id).toBe(refs[1].id);
  });

  test('@not: []', async () => {
    const jql = {
      "id@not": [refs[0].id, refs[1].id]
    }
    const count = await jqlApi.count();
    const res = await jqlApi.find(jql);
    const characters = res.content;
    expect(characters.length).toBe(count - refs.length)
    const id_set = {};
    for (const character of characters) {
      id_set[character.id] = 0;
    }
    expect(id_set[refs[0].id]).toBeUndefined();
    expect(id_set[refs[1].id]).toBeUndefined();
  });

  test('@like []', async () => {
    const jql = {
      "name@like": [refs[0].name.substring(0, 4) + "%", refs[1].name.substring(0, 4) + "%"] 
    }
    const res = await jqlApi.find(jql);
    const characters = res.content;
    expect(characters.length).toBeGreaterThanOrEqual(refs.length)
    const id_set = {};
    for (const character of characters) {
      id_set[character.id] = 0;
    }
    expect(id_set[refs[0].id]).not.toBeUndefined();
    expect(id_set[refs[1].id]).not.toBeUndefined();
  });

  test('@not like []', async () => {
    const jql = {
      "name@not like": [refs[0].name.substring(0, 4) + "%", refs[1].name.substring(0, 4) + "%"] 
    }
    const res = await jqlApi.find(jql);
    const characters = res.content;
    expect(characters.length).toBeGreaterThanOrEqual(refs.length)
    const id_set = {};
    for (const character of characters) {
      id_set[character.id] = 0;
    }
    expect(id_set[refs[0].id]).toBeUndefined();
    expect(id_set[refs[1].id]).toBeUndefined();
  });
});

