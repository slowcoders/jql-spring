import {describe, expect, test} from '@jest/globals';
import { jqlApi } from '@/api/jqlApi'

describe('Top', () => {
  test('Find first', async () => {
    const jql = {
      "name@like": "Luke%"
    }
    const character = await jqlApi.top(jql);

    expect(character.name).toBe("Luke Skywalker");

    expect(character.name).toMatch("Sky");
    expect(character.name).toMatch(/Luke Skywalker/);
    expect(character.name).toMatch(/Luke .*/);

    expect(character.name.indexOf("Luke") == 0).toBeTruthy();
    expect(character.name.startsWith("Luke")).toBeTruthy();
  });

  test('Select ID only', async () => {
    const character = await jqlApi.top(null, { select: "0" });
    expect(character.id).not.toBeUndefined();
    expect(character.name).toBeUndefined();
  });

  test('Find any character having a starship that length > 10', async () => {
    const jql = {
      "starship": { "length@gt": 10 }
    }
    const character = await jqlApi.top(jql);
    for (const ship of character.starship) {
      expect(ship.length).toBeGreaterThan(10);
    }
  });

  test('Find any character having a starship that length < 10', async () => {
    const jql = {
      "starship": { "length@lt": 10 }
    }
    const character = await jqlApi.top(jql);
    for (const ship of character.starship) {
      expect(ship.length).toBeLessThan(10);
    }
  });
});

