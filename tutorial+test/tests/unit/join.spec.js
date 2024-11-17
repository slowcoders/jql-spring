import {describe, expect, test} from '@jest/globals';
import { customerRepo } from '@/sample_db'

describe('Join Test', () => {
  describe('Advanced Join', () => {
    test('Find friends of Han Solo', async () => {
      const filter = {
        "name" : "Han Solo",
        "friend_": {} 
      }      
      const res = await customerRepo.find(filter);
      const customers = res.content;
      // console.log(res.content)
      expect(customers.length).toBe(1);
      expect(customers[0].friend_.length).toBeGreaterThanOrEqual(3);
    });

    const PRICE = 15000;
    test('Find friends of Han Solo who ordered book price > 15000', async () => {
      const filter = {
        "name" : "Han Solo",
        "friend_": { "book_": { "price@gt": PRICE } }
      }
      const res = await customerRepo.find(filter);
      const customers = res.content;
      expect(customers.length).toBe(1);
      expect(customers[0].friend_.length).toBe(1);
    });
  });
});

