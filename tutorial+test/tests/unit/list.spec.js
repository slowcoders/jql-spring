import {describe, expect, test} from '@jest/globals';
import { studentRepo } from '@/sample_db'

function checkSorted(items, key, ascending) {
  let prev = items[0];
  for (let i = 1; i < items.length; i++) {
    const item = items[i];
    if (ascending) {
      expect(item[key].localeCompare(prev[key]) >= 0).toBeTruthy();
    } else {
      expect(item[key].localeCompare(prev[key]) <= 0).toBeTruthy();
    }
    prev = item;
  }
}

describe('Listing', () => {
  test('Find All', async () => {
    const count = await studentRepo.count();
    const res = await studentRepo.find();
    const students = res.content;
    expect(students.length).toBe(count);
  });

  test('Sort by name ascending order', async () => {
    const res = await studentRepo.find(null, { sort: "name" });
    const students = res.content;
    expect(students.length).toBeGreaterThan(0);
    checkSorted(students, "name", true);
  });

  test('Sort by name descending order', async () => {
    const res = await studentRepo.find(null, { sort: "-name" });
    const students = res.content;
    expect(students.length).toBeGreaterThan(0);
    checkSorted(students, "name", false);
  });

  test('Limit & Sort', async () => {
    const limit = 5
    const res = await studentRepo.find(null, { sort: "-name", limit });
    const students = res.content;
    expect(students.length).toBe(limit);
    checkSorted(students, "name", false);
  });

  test('Pagination & Sort', async () => {
    const limit = 3;
    const page = 1;
    const res = await studentRepo.find(null, { sort: "-name", limit, page });
    const students = res.content;
    expect(students.length).toBeLessThanOrEqual(limit);
    expect(res.metadata.totalElements).toBeGreaterThanOrEqual(limit);
    checkSorted(students, "name", false);
  });

});

