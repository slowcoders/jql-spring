import { HqlApi } from "./api/hqlApi";

const starwars_url = "http://localhost:7007/api/hql/starwars"
const authorRepo = new HqlApi(`${starwars_url}/author`);
const episodeRepo = new HqlApi(`${starwars_url}/episode`);
const bookRepo = new HqlApi(`${starwars_url}/book`);
const author_episode_repo = new HqlApi(`${starwars_url}/author_episode_link`);
const author_friend_repo = new HqlApi(`${starwars_url}/author_friend_link`);

export async function initSampleDB() {
    // if (await authorRepo.count() > 0) return;
    const authors = await authorRepo.insertAll(default_authors, 'ignore');
    console.log(authors);
    const episodes   = await episodeRepo.insertAll(default_episodes, 'ignore');
    console.log(episodes);
    const books  = await bookRepo.insertAll(default_books, 'ignore');
    console.log(books);
    const author_episode_links = await author_episode_repo.insertAll(default_author_episode_links, 'ignore');
    console.log(author_episode_links);
    const author_friend_links = await author_friend_repo.insertAll(default_friend_map, 'ignore');
    console.log(author_friend_links);
}

const default_authors = [
    {
        "id": 1000,
        "species": "Human",
        "name": "Luke Skywalker",
        "height": 1.72,
        "mass": 77,
        "metadata": {
            "memo": {
                "shoeSize": 260,
                "favoriteFood": "kimchi"
            },
            "homePlanet": "Tatooine"
        }
    },
    {
        "id": 1001,
        "species": "Human",
        "name": "Darth Vader",
        "height": 2.02,
        "mass": 136,
        "metadata": {
            "memo": {
                "shoeSize": 370,
                "favoriteFood": "pork"
            },
            "homePlanet": "Tatooine"
        }
    },
    {
        "id": 1003,
        "species": "Human",
        "name": "Leia Organa",
        "height": 1.5,
        "mass": 49,
        "metadata": {
            "homePlanet": "Alderaan"
        }
    },
    {
        "id": 1004,
        "species": "Human",
        "name": "Wilhuff Tarkin",
        "height": 1.8,
        "mass": null,
        "metadata": {
            "memo": {
                "shoeSize": 350,
                "favoriteFood": "fish"
            }
        }
    },
    {
        "id": 1005,
        "species": "Human",
        "name": "Extra-1",
        "height": 1.19,
        "mass": 50,
        "metadata": null
    },
    {
        "id": 1006,
        "species": "Human",
        "name": "Extra-2",
        "height": 1.8,
        "mass": 121,
        "metadata": null
    },
    {
        "id": 2000,
        "species": "Droid",
        "name": "C-3PO",
        "height": 1.71,
        "mass": 75,
        "metadata": {
            "primaryFunction": "protocol"
        }
    },
    {
        "id": 2001,
        "species": "Droid",
        "name": "R2-D2",
        "height": 1.09,
        "mass": 32,
        "metadata": {
            "primaryFunction": "Astromech"
        }
    },
    {
        "id": 1002,
        "species": "Human",
        "name": "Han Solo",
        "height": 1.8,
        "mass": 80,
        "metadata": {
            "hobby": [
                "축구",
                "야구",
                "영화",
                "만화"
            ],
            "birth-day": "2001.03.03"
        }
    }
];

const default_books = [
    {
        "id": 3000,
        "length": 34.37,
        "name": "Millenium Falcon",
        "author_id": 1002,
    },
    {
        "id": 3001,
        "length": 12.5,
        "name": "X-Wing",
        "author_id": 1000
    },
    {
        "id": 3002,
        "length": 9.2,
        "name": "TIE Advanced x1",
        "author_id": 1001
    },
    {
        "id": 3003,
        "length": 20,
        "name": "Imperial shuttle"
    }
]
  

const default_episodes = [
    {
        "title": "NEWHOPE",
        "published": "2022-12-09T15:00:00"
    },
    {
        "title": "EMPIRE",
        "published": "2021-12-09T15:00:00"
    },
    {
        "title": "JEDI",
        "published": "2020-12-09T15:00:00"
    }
]

const default_author_episode_links = [
    {  author_id: 1000, episode_id: 'NEWHOPE' },
    {  author_id: 1000, episode_id: 'EMPIRE' },
    {  author_id: 1000, episode_id: 'JEDI' },
    {  author_id: 1001, episode_id: 'NEWHOPE' },
    {  author_id: 1001, episode_id: 'EMPIRE' },
    {  author_id: 1001, episode_id: 'JEDI' },
    {  author_id: 1002, episode_id: 'NEWHOPE' },
    {  author_id: 1002, episode_id: 'EMPIRE' },
    {  author_id: 1002, episode_id: 'JEDI' },
    {  author_id: 1003, episode_id: 'NEWHOPE' },
    {  author_id: 1003, episode_id: 'EMPIRE' },
    {  author_id: 1003, episode_id: 'JEDI' },
    {  author_id: 1004, episode_id: 'NEWHOPE' },
    {  author_id: 2000, episode_id: 'NEWHOPE' },
    {  author_id: 2000, episode_id: 'EMPIRE' },
    {  author_id: 2000, episode_id: 'JEDI' },
    {  author_id: 2001, episode_id: 'NEWHOPE' },
    {  author_id: 2001, episode_id: 'EMPIRE' },
    {  author_id: 2001, episode_id: 'JEDI'}
];

const default_friend_map = [
    {  author_id: 1000, friend_id: 1002 },
    {  author_id: 1000, friend_id: 1003 },
    {  author_id: 1000, friend_id: 2000 },
    {  author_id: 1000, friend_id: 2001 },

    {  author_id: 1001, friend_id: 1004 },

    {  author_id: 1002, friend_id: 1000 },
    {  author_id: 1002, friend_id: 1003 },
    {  author_id: 1002, friend_id: 2001 },

    {  author_id: 1003, friend_id: 1000 },
    {  author_id: 1003, friend_id: 1002 },
    {  author_id: 1003, friend_id: 2000 },
    {  author_id: 1003, friend_id: 2001 },

    {  author_id: 1004, friend_id: 1001 },

    {  author_id: 2000, friend_id: 1000 },
    {  author_id: 2000, friend_id: 1002 },
    {  author_id: 2000, friend_id: 1003 },
    {  author_id: 2000, friend_id: 2001 },

    {  author_id: 2001, friend_id: 1000 },
    {  author_id: 2001, friend_id: 1002 },
    {  author_id: 2001, friend_id: 1003 }
]


const g_serviceUrl = 'http://localhost:7007/api/hql/starwars/';
export const hqlApi = new HqlApi(g_serviceUrl + 'author');
