import {describe, expect, test} from '@jest/globals';
import { hqlApi } from '@/sample_db'

describe('Join Test', () => {
  describe('Advanced Join', () => {
    test('Find friends of Han Solo', async () => {
      const filter = {
        "name" : "Han Solo",
        "friend_": {} 
      }      
      const res = await hqlApi.find(filter);
      const authors = res.content;
      console.log(res.content)
      expect(authors.length).toBe(1);
      expect(authors[0].friend_.length).toBeGreaterThanOrEqual(3);
    });

    test('Find friends of Han Solo with joined query', async () => {
      const filter = {
        "name" : "Han Solo",
        "friend_": { "book_": { "length@ge": 10 } }
      }
      const res = await hqlApi.find(filter);
      const authors = res.content;
      expect(authors.length).toBe(1);
      expect(authors[0].friend_.length).toBe(1);
    });
  });
});

