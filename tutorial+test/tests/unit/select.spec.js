import { describe, expect, test } from '@jest/globals';
import { customerRepo } from '@/sample_db'

describe('Top', () => {
  test('Find first', async () => {
    const filter = {
      "name@like": "Luke%"
    }
    const customer = await customerRepo.top(filter);

    expect(customer.name).toBe("Luke Skywalker");

    expect(customer.name).toMatch("Sky");
    expect(customer.name).toMatch(/Luke Skywalker/);
    expect(customer.name).toMatch(/Luke .*/);

    expect(customer.name.indexOf("Luke") == 0).toBeTruthy();
    expect(customer.name.startsWith("Luke")).toBeTruthy();
  });

  test('Select PrimaryKeys only', async () => {
    const customer = await customerRepo.top(null, { select: "0" });
    for (const k in customer) {
      expect(k).toBe('id');
    }
  });

  test('Select Name and PrimaryKey(Auto selected)', async () => {
    const customer = await customerRepo.top(null, { select: "name" });
    for (const k in customer) {
      expect(k == 'id' || k == 'name').toBeTruthy();
    }
  });

  test('Select Name and PrimaryKeys(Explicitly selected)', async () => {
    const customer = await customerRepo.top(null, { select: "0, name" });
    expect(customer.id).not.toBeUndefined();
    expect(customer.name).not.toBeUndefined();
    for (const k in customer) {
      expect(k == 'id' || k == 'name').toBeTruthy();
    }
  });

  const PRICE = 13500;
  // sum 으로 변경.
  test('Find any customer order a book that price >= ' + PRICE, async () => {
    const filter = {
      "book_": { "price@ge": PRICE }
    }
    const customer = await customerRepo.top(filter, {select: "book_"});
    for (const book of customer.book_) {
      expect(book.price).toBeGreaterThanOrEqual(PRICE);
    }
  });

  test('Find any customer order a book that price < ' + PRICE, async () => {
    const filter = {
      "book_": { "price@lt": PRICE }
    }
    const customer = await customerRepo.top(filter, {select: "book_"});
    for (const book of customer.book_) {
      expect(book.price).toBeLessThan(PRICE);
    }
  });
});

