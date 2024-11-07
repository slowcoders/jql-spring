import {describe, expect, test} from '@jest/globals';
import { hqlApi } from '@/sample_db'

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
    const count = await hqlApi.count();
    const res = await hqlApi.find();
    const authors = res.content;
    expect(authors.length).toBe(count);
  });

  test('Sort by name ascending order', async () => {
    const res = await hqlApi.find(null, { sort: "name" });
    const authors = res.content;
    expect(authors.length).toBeGreaterThan(0);
    checkSorted(authors, "name", true);
  });

  test('Sort by name descending order', async () => {
    const res = await hqlApi.find(null, { sort: "-name" });
    const authors = res.content;
    expect(authors.length).toBeGreaterThan(0);
    checkSorted(authors, "name", false);
  });

  test('Limit & Sort', async () => {
    const limit = 5
    const res = await hqlApi.find(null, { sort: "-name", limit });
    const authors = res.content;
    expect(authors.length).toBe(limit);
    checkSorted(authors, "name", false);
  });

  test('Pagination & Sort', async () => {
    const limit = 3;
    const page = 1;
    const res = await hqlApi.find(null, { sort: "-name", limit, page });
    const authors = res.content;
    expect(authors.length).toBeLessThanOrEqual(limit);
    expect(res.metadata.totalElements).toBeGreaterThanOrEqual(limit);
    checkSorted(authors, "name", false);
  });

});

