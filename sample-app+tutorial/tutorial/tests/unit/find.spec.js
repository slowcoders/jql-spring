import {describe, expect, test} from '@jest/globals';
import { jqlApi } from '@/api/jqlApi'

describe('Join Test', () => {
  describe('Advanced Join', () => {
    test('Find friends of Han Solo', async () => {
      const jql = {
        "name" : "Han Solo",
        "+friend<name>": {} 
      }      
      const res = await jqlApi.find(jql);
      const characters = res.content;
      expect(characters.length).toBe(1);
      expect(characters[0]["+friend"].length).toBeGreaterThanOrEqual(3);
    });

    test('Find friends of Han Solo with joined query', async () => {
      const jql = {
        "name" : "Han Solo",
        "+friend<name>": { "starship<name, @>": { "length@ge": 10 } }
      }
      const res = await jqlApi.find(jql);
      const characters = res.content;
      expect(characters.length).toBe(1);
      expect(characters[0]["+friend"].length).toBe(1);
    });
  });
});

