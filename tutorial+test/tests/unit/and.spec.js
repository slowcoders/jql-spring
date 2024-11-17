import {beforeAll, describe, expect, test} from '@jest/globals';
import { studentRepo } from '@/sample_db'

describe('And operation', () => {
  let last_count;
  const filter = {}

  beforeAll(async () => {
    const res = await studentRepo.find();
    last_count = res.content.length;

    const res2 = await studentRepo.find(null);
    expect(res2.content.length).toBe(last_count)

    const res3 = await studentRepo.find(filter);
    expect(res3.content.length).toBe(last_count)
  })

  test.each([
    { attr: "height@gt", value: 1.2},
    { attr: "height@lt", value: 2.0},
    { attr: "mass@gt", value: 60 },
    { attr: "metadata.memo.shoeSize@lt", value: 300 },
    { attr: "metadata.homePlanet", value: "Tatooine" }
  ]) ('And 조건 테스트', async ({attr, value}) => {
    filter[attr] = value;
    const res = await studentRepo.find(filter);
    const students = res.content;
    expect(students.length).toBeLessThan(last_count);
    last_count = students.length;
  });  
});

