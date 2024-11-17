import {beforeAll, describe, expect, test} from '@jest/globals';
import { studentRepo } from '@/sample_db'

describe('Comapre', () => {
  let refs;

  beforeAll(async () => {
    const filter = {}
    const res = await studentRepo.find(filter, {limit: 2, sort: "id" });
    refs = res.content;
    expect(refs.length).toBe(2);
  })

  test('@is: [] (default)', async () => {
    const filter = {
      "id": [refs[0].id, refs[1].id]
    }
    const res = await studentRepo.find(filter, { sort: "id" });
    const students = res.content;
    expect(students.length).toBe(refs.length);
    expect(students[0].id).toBe(refs[0].id);
    expect(students[1].id).toBe(refs[1].id);
  });

  test('@is: [] (explicit)', async () => {
    const filter = {
      "id": [refs[0].id, refs[1].id]
    }
    const res = await studentRepo.find(filter, { sort: "id" });
    const students = res.content;
    expect(students.length).toBe(refs.length);
    expect(students[0].id).toBe(refs[0].id);
    expect(students[1].id).toBe(refs[1].id);
  });

  test('@not: []', async () => {
    const filter = {
      "id@not": [refs[0].id, refs[1].id]
    }
    const count = await studentRepo.count();
    const res = await studentRepo.find(filter);
    const students = res.content;
    expect(students.length).toBe(count - refs.length)
    const id_set = {};
    for (const student of students) {
      id_set[student.id] = 0;
    }
    expect(id_set[refs[0].id]).toBeUndefined();
    expect(id_set[refs[1].id]).toBeUndefined();
  });

  test('@like []', async () => {
    const filter = {
      "name@like": [refs[0].name.substring(0, 4) + "%", refs[1].name.substring(0, 4) + "%"] 
    }
    const res = await studentRepo.find(filter);
    const students = res.content;
    expect(students.length).toBeGreaterThanOrEqual(refs.length)
    const id_set = {};
    for (const student of students) {
      id_set[student.id] = 0;
    }
    expect(id_set[refs[0].id]).not.toBeUndefined();
    expect(id_set[refs[1].id]).not.toBeUndefined();
  });

  test('@not like []', async () => {
    const filter = {
      "name@not like": [refs[0].name.substring(0, 4) + "%", refs[1].name.substring(0, 4) + "%"] 
    }
    const res = await studentRepo.find(filter);
    const students = res.content;
    expect(students.length).toBeGreaterThanOrEqual(refs.length)
    const id_set = {};
    for (const student of students) {
      id_set[student.id] = 0;
    }
    expect(id_set[refs[0].id]).toBeUndefined();
    expect(id_set[refs[1].id]).toBeUndefined();
  });
});

