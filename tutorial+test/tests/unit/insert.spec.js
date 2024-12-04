import {beforeAll, describe, expect, test} from '@jest/globals';
import { bookRepo } from '@/sample_db'

let new_book_id = 4000;
function next_book_id() {
  return ++new_book_id;
}

describe('Insert/Delete test', () => {

  async function clear_garbage(filter) {
    const garbage = (await bookRepo.find(filter)).content;
    if (garbage.length > 0) {
      await bookRepo.delete(garbage.map(row => row.id))
    }
  }

  describe('single entity insert/delete test',  () => {
    const book_data = {
      id: next_book_id(),
      title: "Test-E1",
      price: 15000,
    }
    let book;

    beforeAll(async () => {
      await clear_garbage({ title: book_data.title });
      book = (await bookRepo.insert(book_data)).content;
    });

    test('insert', async () => {
      expect(book.title).toBe(book_data.title);
      // expect(new Date(book.published)).toBe(entities[0].published)
    });

    test('should throw error on conflict', async () => {
      await expect(async () => {
        book = (await bookRepo.insert(book)).content;
      }).rejects.toThrowError();
    });

    test('delete test entity', async () => {
      await bookRepo.delete(book.id);
    });

  });


  describe('batch insert/delete test', () => {

    const entity_data = [
      {
        id: next_book_id(),
        title: "Test-E2-1",
        price: 25000,
      }, {
        id: next_book_id(),
        title: "Test-E2-2",
        price: 30000,
      }
    ];

    let book_map = {};

    beforeAll(async () => {
      await clear_garbage({ 'title like': 'Test-E2-%' });

      const books = (await bookRepo.insertAll(entity_data)).content;
      expect(books.length).toBe(entity_data.length);
      for (const book of books) {
        book_map[book.title] = book;
      }
    })

    test('Insert new', async () => {
      for (const book of entity_data) {
        expect(book.title).toBe(book_map[book.title].title);
        // expect(book.published).toBe(book_map[idx].published)
      }
    });

    test('should throw error on conflict', async () => {
      await expect(async () => {
        const book = await bookRepo.insertAll(entity_data);
      }).rejects.toThrowError();
    });

    test('Ignore on conflict', async () => {
      const books = (await bookRepo.insertAll(entity_data, 'ignore')).content;
      for (const book of books) {
        expect(book.title).toBe(book_map[book.title].title);
        expect(book.id).toBe(book_map[book.title].id);
        // expect(book.published).toBe(book_map[idx].published)
      }
    });

    test('Insert or Update', async () => {
      for (const book of entity_data) {
        book.price = 25000
      }
      const books = (await bookRepo.insertAll(entity_data, 'update')).content;
      for (const book of books) {
        expect(book.title).toBe(book_map[book.title].title);
        expect(book.id).toBe(book_map[book.title].id);
        // expect(book.published).toBe(book_map[idx].published)
      }
    });

    test('delete it', async () => {
      const titles = Object.keys(book_map);
      let books = (await bookRepo.find({ title: titles } )).content;
      expect(books.length).toBe(titles.length);

      await bookRepo.delete(books.map(row => row.id));

      books = (await bookRepo.find({ title: titles } )).content;
      expect(books.length).toBe(0)
    });
  });
});

