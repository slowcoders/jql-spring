import { shallowMount } from '@vue/test-utils'
import { jqlApi } from '@/api/jqlApi'

describe('Join Test', () => {
  describe('Advanced Join', () => {
    it('Find friends of Han Solo', async () => {
      const jql = {
        "name" : "Han Solo",
        "+friend<name>": { "starship<name, @>": { "length@ge": 10 } }
      }      
      const result = await jqlApi.find(jql);
      expect(result.length).toBe(1);
      expect(result["+freinds"].length).toBeGreaterThanOrEqual(4);
    });
  });
});

