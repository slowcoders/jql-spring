import {beforeAll, describe, expect, test} from '@jest/globals';
import { customerRepo } from '@/sample_db'

const normal_height = [ 1.2, 2.0 ]

const normal_mass = [ 40, 120 ]

const MIN = 0, MAX = 1;

describe('Or operation', () => {
  let last_count;

  const too_small_or_too_tall = {
    "@or": {
      "height@lt": normal_height[MIN],
      "height@gt": normal_height[MAX]
    }
  }

  test('too_small_or_too_tall', async () => {
    const res = await customerRepo.find(too_small_or_too_tall);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.height < normal_height[MIN] || customer.height > normal_height[MAX]).toBeTruthy();
    }
  });

  const too_light_or_too_heavy = {
    "@or": {
      "mass@lt": normal_mass[MIN],
      "mass@gt": normal_mass[MAX]
    }
  }

  test('too_light_or_too_heavy', async () => {
    const res = await customerRepo.find(too_light_or_too_heavy);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.mass < normal_mass[MIN] || customer.mass > normal_mass[MAX]).toBeTruthy();
    }
  });


  const too_small_or_too_heavy = {
    "@or": {
      "height@lt": normal_height[MIN],
      "mass@gt": normal_mass[MAX]
    }
  }

  test('too_small_or_too_heavy', async () => {
    const res = await customerRepo.find(too_small_or_too_heavy);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.height < normal_height[MIN] || customer.mass > normal_mass[MAX]).toBeTruthy();
    }
  });

  const too_small_or_too_tall__AND__too_light_or_too_heavy = {
    "@and": [
      too_small_or_too_tall,
      too_light_or_too_heavy
    ]
  }
  test('too_small_or_too_tall__AND__too_light_or_too_heavy', async () => {
    const res = await customerRepo.find(too_small_or_too_tall__AND__too_light_or_too_heavy);
    const customers = res.content;
    for (const customer of customers) {
      expect((customer.height < normal_height[MIN] || customer.height > normal_height[MAX])
          &&        (customer.mass < normal_mass[MIN] || customer.mass > normal_mass[MAX]) ).toBeTruthy();
    }
  });
});

