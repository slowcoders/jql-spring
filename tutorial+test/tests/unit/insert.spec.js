import {beforeAll, describe, expect, test} from '@jest/globals';
import { HqlApi } from '@/api/hqlApi'

const hqlApi = new HqlApi('http://localhost:7007/api/hql/starwars/episode')
describe('Insert/Delete test', () => {

  async function clear_garbage(filter) {
    const garbage = (await hqlApi.find(filter)).content;
    let ids = '';
    if (garbage.length > 0) {
      for (const episode of garbage) {
        if (ids.length > 0) ids += ','
        ids += episode.title;
      }
      await hqlApi.delete(ids)
    }
  }

  describe('single entity insert/delete test',  () => {
    const episode_data = {
      title: "Test-E1",
      published: ("2023-03-23 10:30:00"),
    }
    let episode;

    beforeAll(async () => {
      await clear_garbage({ title: episode_data.title });
      episode = (await hqlApi.insert(episode_data)).content;
    });

    test('insert', async () => {
      expect(episode.title).toBe(episode_data.title);
      // expect(new Date(episode.published)).toBe(entities[0].published)
    });

    test('should throw error on conflict', async () => {
      await expect(async () => {
        const episode = (await hqlApi.insert(episode)).content;
      }).rejects.toThrowError();
    });

    test('delete test entity', async () => {
      await hqlApi.delete(episode.title);
    });

  });


  describe('batch insert/delete test', () => {

    const entity_data = [
      {
        title: "Test-E2-1",
        published: ("2023-03-23 10:30:00"),
      }, {
        title: "Test-E2-2",
        published: ("2023-03-23 10:30:00"),
      }
    ];

    let episode_map = {};

    beforeAll(async () => {
      await clear_garbage({ 'title@like': 'Test-E2-%' });

      const idList = (await hqlApi.insertAll(entity_data)).content;
      const res = await hqlApi.find({title: idList})
      const episodes = res.content;
      expect(episodes.length).toBe(entity_data.length);
      for (const episode of episodes) {
        episode_map[episode.title] = episode;
      }
    })

    test('Insert new', async () => {
      for (const episode of entity_data) {
        expect(episode.title).toBe(episode_map[episode.title].title);
        // expect(episode.published).toBe(episode_map[idx].published)
      }
    });

    test('should throw error on conflict', async () => {
      await expect(async () => {
        const episode = await hqlApi.insertAll(entity_data);
      }).rejects.toThrowError();
    });

    test('Ignore on conflict', async () => {
      const episodes = (await hqlApi.insertAll(entity_data, 'ignore')).content;
      for (const episode of episodes) {
        expect(episode.title).toBe(episode_map[episode.title].title);
        expect(episode.id).toBe(episode_map[episode.title].id);
        // expect(episode.published).toBe(episode_map[idx].published)
      }
    });

    test('Insert or Update', async () => {
      for (const episode of entity_data) {
        episode.published = "2023-03-31 10:30:00"
      }
      const episodes = (await hqlApi.insertAll(entity_data, 'update')).content;
      for (const episode of episodes) {
        expect(episode.title).toBe(episode_map[episode.title].title);
        expect(episode.id).toBe(episode_map[episode.title].id);
        // expect(episode.published).toBe(episode_map[idx].published)
      }
    });

    test('delete it', async () => {
      const titles = Object.keys(episode_map);
      let episodes = (await hqlApi.find({ title: titles } )).content;
      expect(episodes.length).toBe(titles.length);

      await hqlApi.delete(titles);

      episodes = (await hqlApi.find({ title: titles } )).content;
      expect(episodes.length).toBe(0)
    });
  });
});

