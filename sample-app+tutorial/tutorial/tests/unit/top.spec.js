import {describe, expect, test} from '@jest/globals';
import { jqlApi } from '@/api/jqlApi'

describe('Top', () => {
  test('Find first', async () => {
    const jql = {
      "name@like": "Luke%"
    }
    const result = await jqlApi.top(jql);
    expect(result.name).toContain("Luke");
  });

  test('Find first having a starship that length > 10', async () => {
    const jql = {
      "starship": { "length@gt": 10 }
    }
    const result = await jqlApi.top(jql);
    expect(result.length).toBeGreaterThan(10);
  });

  test('Find first having a starship that length < 10', async () => {
    const jql = {
      "starship": { "length@lt": 10 }
    }
    const result = await jqlApi.top(jql);
    expect(result.length).toBeLessThan(10);
  });
});

