import {describe, expect, test} from '@jest/globals';
import { studentRepo } from '@/sample_db'

describe('Join Test', () => {
  describe('Advanced Join', () => {
    test('Find friends of Han Solo', async () => {
      const filter = {
        "name" : "Han Solo",
        "friend_": {} 
      }      
      const res = await studentRepo.find(filter);
      const students = res.content;
      expect(students.length).toBe(1);
      expect(students[0].friend_.length).toBeGreaterThanOrEqual(3);
    });

    const PRICE = 15000;
    test('Find friends of Han Solo who ordered book price > ' + PRICE, async () => {
      const filter = {
        "name" : "Han Solo",
        "friend_": { "book_": { "price@gt": PRICE } }
      }
      const res = await studentRepo.find(filter);
      const students = res.content;
      expect(students.length).toBe(1);
      expect(students[0].friend_.length).toBe(1);
    });
  });
});

