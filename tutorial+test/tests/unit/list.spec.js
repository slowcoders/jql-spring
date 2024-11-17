import {describe, expect, test} from '@jest/globals';
import { customerRepo } from '@/sample_db'

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
    const count = await customerRepo.count();
    const res = await customerRepo.find();
    const customers = res.content;
    expect(customers.length).toBe(count);
  });

  test('Sort by name ascending order', async () => {
    const res = await customerRepo.find(null, { sort: "name" });
    const customers = res.content;
    expect(customers.length).toBeGreaterThan(0);
    checkSorted(customers, "name", true);
  });

  test('Sort by name descending order', async () => {
    const res = await customerRepo.find(null, { sort: "-name" });
    const customers = res.content;
    expect(customers.length).toBeGreaterThan(0);
    checkSorted(customers, "name", false);
  });

  test('Limit & Sort', async () => {
    const limit = 5
    const res = await customerRepo.find(null, { sort: "-name", limit });
    const customers = res.content;
    expect(customers.length).toBe(limit);
    checkSorted(customers, "name", false);
  });

  test('Pagination & Sort', async () => {
    const limit = 3;
    const page = 1;
    const res = await customerRepo.find(null, { sort: "-name", limit, page });
    const customers = res.content;
    expect(customers.length).toBeLessThanOrEqual(limit);
    expect(res.metadata.totalElements).toBeGreaterThanOrEqual(limit);
    checkSorted(customers, "name", false);
  });

});

