import {beforeAll, describe, expect, test} from '@jest/globals';
import { JqlApi } from '@/api/jqlApi'

const jqlApi = new JqlApi('http://localhost:7007/api/jql/starwars/episode')
describe('Insert/Delete test', () => {

  const entities = [
    {
      title: "Tester1",
      published: ("2023-03-23T10:30:00+00:00"),
    }, {
      title: "Tester2",
      published: ("2023-03-23T10:30:00+00:00"),
    }
  ];
  
  async function clear_garbage() {
    const garbage = (await jqlApi.find({ 'published@ge' : '2023-03-22'})).content;
    let ids = '';
    if (garbage.length > 0) {
      for (const episode of garbage) {
        if (ids.length > 0) ids += ','
        ids += episode.title;
      }
      await jqlApi.delete(ids)
    }
  }

  describe('single entity insert/delete test',  () => {
    let episode;

    beforeAll(async () => {
      await clear_garbage();
      episode = await jqlApi.insert(entities[0]);
    });

    test('insert', async () => {
      expect(episode.title).toBe(entities[0].title);
      // expect(new Date(episode.published)).toBe(entities[0].published)
    });

    test('should throw error on conflict', async () => {
      await expect(async () => {
        const episode = await jqlApi.insert(episode);
      }).rejects.toThrowError();
    });

    test('delete test entity', async () => {
      await jqlApi.delete(episode.title);
    });

  });


  describe('batch insert/delete test', () => {

    let episode_map = {};

    beforeAll(async () => {
      await clear_garbage();

      const idList = await jqlApi.insertAll(entities);
      const res = await jqlApi.find({title: idList})
      const episodes = res.content;
      expect(episodes.length).toBe(entities.length);
      for (const episode of episodes) {
        episode_map[episode.title] = episode;
      }
    })

    test('Insert new', async () => {
      for (const episode of entities) {
        expect(episode.title).toBe(episode_map[episode.title].title);
        // expect(episode.published).toBe(episode_map[idx].published)
      }
    });

    test('should throw error on conflict', async () => {
      await expect(async () => {
        const episode = await jqlApi.insertAll(entities);
      }).rejects.toThrowError();
    });

    test('Ignore on conflict', async () => {
      const idList = await jqlApi.insertAll(entities, 'ignore');
      const res = await jqlApi.find({title: idList})
      const episodes = res.content;
      for (const episode of episodes) {
        expect(episode.title).toBe(episode_map[episode.title].title);
        expect(episode.id).toBe(episode_map[episode.title].id);
        // expect(episode.published).toBe(episode_map[idx].published)
      }
    });

    test('Insert or Update', async () => {
      for (const episode of entities) {
        episode.published = "2023-03-31 10:30:00"
      }
      const idList = await jqlApi.insertAll(entities, 'update');
      const res = await jqlApi.find({title: idList})
      const episodes = res.content;
      for (const episode of episodes) {
        expect(episode.title).toBe(episode_map[episode.title].title);
        expect(episode.id).toBe(episode_map[episode.title].id);
        // expect(episode.published).toBe(episode_map[idx].published)
      }
    });

    test('delete it', async () => {
      const titles = Object.keys(episode_map);
      let episodes = (await jqlApi.find({ title: titles } )).content;
      expect(episodes.length).toBe(titles.length);

      await jqlApi.delete(titles);

      episodes = (await jqlApi.find({ title: titles } )).content;
      expect(episodes.length).toBe(0)
    });
  });
});

