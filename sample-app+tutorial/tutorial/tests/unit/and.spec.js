import {beforeAll, describe, expect, test} from '@jest/globals';
import { jqlApi } from '@/api/jqlApi'

describe('And operation', () => {
  let last_count;
  const filter = {}

  beforeAll(async () => {
    const filter = {}
    last_count = await jqlApi.count();
  })

  test.each([
    { attr: "species", value: "Human"},
    { attr: "height@gt", value: 1.2},
    { attr: "height@lt", value: 2.0},
    { attr: "mass@gt", value: 60 },
    { attr: "metadata.homePlanet", value: "Tatooine" }
  ]) ('And 조건 테스트', async ({attr, value}) => {
    jql[attr] = value;
    const res = await jqlApi.find(jql);
    const characters = res.content;
    expect(characters.length).toBeLessThan(last_count);
    last_count = characters.length;
  });  
});

