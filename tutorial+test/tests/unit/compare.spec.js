import {beforeAll, describe, expect, test} from '@jest/globals';
import { customerRepo } from '@/sample_db'

describe('Comapre', () => {
  let ref;

  beforeAll(async () => {
    const filter = {
      "name": "Luke Skywalker"
    }
    ref = await customerRepo.top(filter);
  })

  test('@is (default)', async () => {
    const filter = {
      "id": ref.id
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBe(1)
  });

  test('@is (explicit)', async () => {
    const filter = {
      "id@is": ref.id
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBe(1)
  });

  test('@not', async () => {
    const filter = {
      "id@not": ref.id
    }
    const count = await customerRepo.count();
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBe(count - 1)
  });

  test('@le (less or equals)', async () => {
    const filter = {
      "id@le": ref.id
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.id).toBeLessThanOrEqual(ref.id)
    }
  });

  test('@lt (less than)', async () => {
    const filter = {
      "id@lt": ref.id
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.id).toBeLessThan(ref.id)
    }
  });

  test('@ge (greater or equals)', async () => {
    const filter = {
      "id@ge": ref.id
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.id).toBeGreaterThanOrEqual(ref.id)
    }
  });

  test('@gt (greater than)', async () => {
    const filter = {
      "id@gt": ref.id
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.id).toBeGreaterThan(ref.id)
    }
  });  

  test('@ge && @le', async () => {
    const filter = {
      "id@ge": ref.id,
      "id@le": ref.id + 1
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    for (const customer of customers) {
      expect(customer.id).toBeGreaterThanOrEqual(ref.id)
      expect(customer.id).toBeLessThanOrEqual(ref.id + 1)
    }
  });    

  test('@between', async () => {
    const filter = {
      "id@between": [ref.id, ref.id + 1]
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.id).toBeGreaterThanOrEqual(ref.id)
      expect(customer.id).toBeLessThanOrEqual(ref.id + 1)
    }
  });    

  test('@not between', async () => {
    const filter = {
      "id@not between": [ref.id, ref.id + 1]
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.id >= ref.id && customer.id <= ref.id + 1).toBeFalsy()
    }
  });    

  test('@like', async () => {
    const name_start = ref.name.substring(0, 4);
    const filter = {
      "name@like": name_start + "%"
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.name).toMatch(name_start)
      expect(customer.name.indexOf(name_start) >= 0).toBeTruthy();
    }
  });    

  test('@not like', async () => {
    const name_start = ref.name.substring(0, 4);
    const filter = {
      "name@not like": name_start + "%"
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.name).not.toMatch(name_start)
      expect(customer.name.indexOf(name_start) >= 0).not.toBeTruthy();
    }
  });

  test('@re', async () => {
    const name_start = ref.name.substring(0, 4);
    const filter = {
      "name@re": name_start + ".*"
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.name).toMatch(name_start)
      expect(customer.name.indexOf(name_start) >= 0).toBeTruthy();
    }
  });

  test('@not re', async () => {
    const name_start = ref.name.substring(0, 4);
    const filter = {
      "name@not re": name_start + ".*"
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.name).not.toMatch(name_start)
      expect(customer.name.indexOf(name_start) >= 0).not.toBeTruthy();
    }
  });

  test('@re/i', async () => {
    const name_start = ref.name.substring(0, 4);
    const filter = {
      "name@re/i": name_start.toUpperCase() + ".*"
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.name).toMatch(name_start)
      expect(customer.name.indexOf(name_start) >= 0).toBeTruthy();
    }
  });

  test('@not re/i', async () => {
    const name_start = ref.name.substring(0, 4);
    const filter = {
      "name@not re/i": name_start.toUpperCase() + ".*"
    }
    const res = await customerRepo.find(filter);
    const customers = res.content;
    expect(customers.length).toBeGreaterThanOrEqual(1);
    for (const customer of customers) {
      expect(customer.name).not.toMatch(name_start)
      expect(customer.name.indexOf(name_start) >= 0).not.toBeTruthy();
    }
  });
});

