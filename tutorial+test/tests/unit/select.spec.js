import { describe, expect, test } from '@jest/globals';
import { studentRepo } from '@/sample_db'

describe('Top', () => {
  test('Find first', async () => {
    const filter = {
      "name@like": "Luke%"
    }
    const student = await studentRepo.top(filter);

    expect(student.name).toBe("Luke Skywalker");

    expect(student.name).toMatch("Sky");
    expect(student.name).toMatch(/Luke Skywalker/);
    expect(student.name).toMatch(/Luke .*/);

    expect(student.name.indexOf("Luke") == 0).toBeTruthy();
    expect(student.name.startsWith("Luke")).toBeTruthy();
  });

  test('Select PrimaryKeys only', async () => {
    const student = await studentRepo.top(null, { select: "0" });
    for (const k in student) {
      expect(k).toBe('id');
    }
  });

  test('Select Name and PrimaryKey(Auto selected)', async () => {
    const student = await studentRepo.top(null, { select: "name" });
    for (const k in student) {
      expect(k == 'id' || k == 'name').toBeTruthy();
    }
  });

  test('Select Name and PrimaryKeys(Explicitly selected)', async () => {
    const student = await studentRepo.top(null, { select: "0, name" });
    expect(student.id).not.toBeUndefined();
    expect(student.name).not.toBeUndefined();
    for (const k in student) {
      expect(k == 'id' || k == 'name').toBeTruthy();
    }
  });

  const PRICE = 13500;
  // sum 으로 변경.
  test('Find any student order a book that price >= ' + PRICE, async () => {
    const filter = {
      "book_": { "price@ge": PRICE }
    }
    const student = await studentRepo.top(filter, {select: "book_"});
    for (const book of student.book_) {
      expect(book.price).toBeGreaterThanOrEqual(PRICE);
    }
  });

  test('Find any student order a book that price < ' + PRICE, async () => {
    const filter = {
      "book_": { "price@lt": PRICE }
    }
    const student = await studentRepo.top(filter, {select: "book_"});
    for (const book of student.book_) {
      expect(book.price).toBeLessThan(PRICE);
    }
  });
});

