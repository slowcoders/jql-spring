import {describe, expect, test} from '@jest/globals';
import { jqlApi } from '@/api/jqlApi'

describe('Join Test', () => {
  describe('Advanced Join', () => {
    test('Find friends of Han Solo', async () => {
      const filter = {
        "name" : "Han Solo",
        "+friend": {} 
      }      
      const res = await jqlApi.find(filter);
      const characters = res.content;
      expect(characters.length).toBe(1);
      expect(characters[0]["+friend"].length).toBeGreaterThanOrEqual(3);
    });

    test('Find friends of Han Solo with joined query', async () => {
      const filter = {
        "name" : "Han Solo",
        "+friend": { "starship": { "length@ge": 10 } }
      }
      const res = await jqlApi.find(filter);
      const characters = res.content;
      expect(characters.length).toBe(1);
      expect(characters[0]["+friend"].length).toBe(1);
    });
  });
});

