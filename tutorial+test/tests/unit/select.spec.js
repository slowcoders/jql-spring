import {describe, expect, test} from '@jest/globals';
import { hqlApi } from '@/api/hqlApi'

// // 문법이 너무 복잡.
// const query = [
//   'a',
//   'b',
//   'c',
//   {
//     "a@eq" : 3,
//     "friend": [
//       '*',
//       {
//         'starship': [
//         ]
//       }
//     ]
//   }
// ]
//
// // Parser 구현 및 @select injection 구현이 가장 단순한다.
// const query2 = {
//   '@select': ['*'],
//   "a@eq" : 3,
//   "friend" : {
//     '@select': [],
//     'starship': []
//   }
// }
//
// // 문법이 가장 깔끔하다.
// // Parser 구현은 어렵지 않으나, select injection 처리 과정이 복잡하다.
// // FrontEnd 에서
// const query3 = {
//   "a@eq" : 3,
//   "friend(#, name)" : {
//     '@select': '',
//     'starship': []
//   }
// }
//
// /*
// Select 와 Filter 를 구분하기.
// Select 문은 구조적으로 정의하고, 검색 조건은 flat 하게 서술하는 것이 FrontEnd 개발자에게 편하다.
// */
// const select4 = "name, friend(name)"
// const query4 = {
//   "friend.name@like": "Luke %"
// }
//
// /*
// Select 와 Filter 를 구분하기 2.
// 좀더 FrontEnd 개발자에게 편할 듯 하지만...
// Query 의 생성과정은 Flat 한 Form 입력을 받은 후, 이를 다시 합성하는 과정이므로 별 차이가 없다.
// */
// const select = "name, friend(name)"
// const query4 = {
//   "friend.name": { "@like": "Luke %" }
// }

describe('Top', () => {
  test('Find first', async () => {
    const filter = {
      "name@like": "Luke%"
    }
    const character = await hqlApi.top(filter);

    expect(character.name).toBe("Luke Skywalker");

    expect(character.name).toMatch("Sky");
    expect(character.name).toMatch(/Luke Skywalker/);
    expect(character.name).toMatch(/Luke .*/);

    expect(character.name.indexOf("Luke") == 0).toBeTruthy();
    expect(character.name.startsWith("Luke")).toBeTruthy();
  });

  test('Select PrimaryKeys only', async () => {
    const character = await hqlApi.top(null, { select: "0" });
    for (const k in character) {
      expect(k).toBe('id');
    }
  });

  test('Select Name and PrimaryKey(Auto selected)', async () => {
    const character = await hqlApi.top(null, { select: "name" });
    for (const k in character) {
      expect(k == 'id' || k == 'name').toBeTruthy();
    }
  });

  test('Select Name and PrimaryKeys(Explicitly selected)', async () => {
    const character = await hqlApi.top(null, { select: "0, name" });
    expect(character.id).not.toBeUndefined();
    expect(character.name).not.toBeUndefined();
    for (const k in character) {
      expect(k == 'id' || k == 'name').toBeTruthy();
    }
  });

  test('Find any character having a starship that length > 10', async () => {
    const filter = {
      "starship_": { "length@gt": 10 }
    }
    const character = await hqlApi.top(filter, {select: "starship_"});
    for (const ship of character.starship_) {
      expect(ship.length).toBeGreaterThan(10);
    }
  });

  test('Find any character having a starship that length < 10', async () => {
    const filter = {
      "starship_": { "length@lt": 10 }
    }
    const character = await hqlApi.top(filter, {select: "starship_"});
    for (const ship of character.starship_) {
      expect(ship.length).toBeLessThan(10);
    }
  });
});

