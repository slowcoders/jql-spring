import { HqlApi } from "./api/hqlApi";

const bookstore_url = "http://localhost:7007/api/hql/bookstore"
export const authorRepo = new HqlApi(`${bookstore_url}/author`);
export const studentRepo = new HqlApi(`${bookstore_url}/student`);
export const episodeRepo = new HqlApi(`${bookstore_url}/episode`);
export const bookRepo = new HqlApi(`${bookstore_url}/book`);
export const bookOrderRepo = new HqlApi(`${bookstore_url}/book_order`);
export const student_episode_repo = new HqlApi(`${bookstore_url}/student_episode_link`);
export const student_friend_repo = new HqlApi(`${bookstore_url}/student_friend_link`);

export async function initSampleDB() {
    // if (await studentRepo.count() > 0) return;
    const authors = await authorRepo.insertAll(default_authors, 'ignore');
    console.log(authors);
    const students = await studentRepo.insertAll(default_students, 'ignore');
    console.log(students);
    const episodes   = await episodeRepo.insertAll(default_episodes, 'ignore');
    console.log(episodes);
    const books  = await bookRepo.insertAll(default_books, 'ignore');
    console.log(books);
    const bookOrders  = await bookOrderRepo.insertAll(default_book_orders, 'ignore');
    console.log(bookOrders);
    const student_episode_links = await student_episode_repo.insertAll(default_student_episode_links, 'ignore');
    console.log(student_episode_links);
    const student_friend_links = await student_friend_repo.insertAll(default_friend_map, 'ignore');
    console.log(student_friend_links);
}

const default_authors = [
    {
        id: 1,
        name: "한강",
    }, {
        id: 2,
        name: "스티븐 킹"
    }, {
        id: 3,
        name: "조엔 롤링"
    }
];

const default_students = [
    {
        "id": 1000,
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
        "name": "Leia Organa",
        "height": 1.5,
        "mass": 49,
        "metadata": {
            "homePlanet": "Alderaan"
        }
    },
    {
        "id": 1004,
        "name": "Groot",
        "height": 1.8,
        "mass": null,
        "metadata": {
            "memo": {
                "shoeSize": 850,
                "favoriteFood": "fish"
            }
        }
    },
    {
        "id": 1005,
        "name": "Hobbit",
        "height": 1.19,
        "mass": 50,
        "metadata": null
    },
    {
        "id": 2000,
        "name": "C-3PO",
        "height": 1.71,
        "mass": 75,
        "metadata": {
            "primaryFunction": "protocol"
        }
    },
    {
        "id": 2001,
        "name": "R2-D2",
        "height": 1.09,
        "mass": 32,
        "metadata": {
            "primaryFunction": "Astromech"
        }
    },
    {
        "id": 1002,
        "name": "Han Solo",
        "height": 1.8,
        "mass": 80,
        "metadata": {
            "memo": {
                "shoeSize": 270,
                "favoriteFood": "wine"
            },
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
        "price": 13500,
        "title": "소년이 온다",
        "author_id": 1,
    },
    {
        "id": 3001,
        "price": 13500,
        "title": "채식주의자",
        "author_id": 1
    },
    {
        "id": 3002,
        "price": 17000,
        "title": "미저리",
        "author_id": 2
    },
    {
        "id": 3003,
        "price": 12900,
        "title": "그린 마일",
        "author_id": 2
    },
    {
        "id": 3004,
        "price": 12500,
        "title": "비밀의 방",
        "author_id": 3
    },
    {
        "id": 3005,
        "price": 12500,
        "title": "불의 잔",
        "author_id": 3
    },
    {
        "id": 3006,
        "price": 12500,
        "title": "죽음의 성물",
        "author_id": 3
    },
    {
        "id": 3007,
        "price": 12500,
        "title": "불사조 기사단",
        "author_id": 3
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

const default_student_episode_links = [
    {  student_id: 1000, episode_id: 'NEWHOPE' },
    {  student_id: 1000, episode_id: 'EMPIRE' },
    {  student_id: 1000, episode_id: 'JEDI' },
    {  student_id: 1001, episode_id: 'NEWHOPE' },
    {  student_id: 1001, episode_id: 'EMPIRE' },
    {  student_id: 1001, episode_id: 'JEDI' },
    {  student_id: 1002, episode_id: 'NEWHOPE' },
    {  student_id: 1002, episode_id: 'EMPIRE' },
    {  student_id: 1002, episode_id: 'JEDI' },
    {  student_id: 1003, episode_id: 'NEWHOPE' },
    {  student_id: 1003, episode_id: 'EMPIRE' },
    {  student_id: 1003, episode_id: 'JEDI' },
    {  student_id: 1004, episode_id: 'NEWHOPE' },
    {  student_id: 2000, episode_id: 'NEWHOPE' },
    {  student_id: 2000, episode_id: 'EMPIRE' },
    {  student_id: 2000, episode_id: 'JEDI' },
    {  student_id: 2001, episode_id: 'NEWHOPE' },
    {  student_id: 2001, episode_id: 'EMPIRE' },
    {  student_id: 2001, episode_id: 'JEDI'}
];

const default_book_orders = [
    {  student_id: 1000, book_id: 3000 },
    {  student_id: 1000, book_id: 3001 },
    {  student_id: 1000, book_id: 3002 },
    {  student_id: 1001, book_id: 3003 },
    {  student_id: 1001, book_id: 3004 },
    {  student_id: 1001, book_id: 3007 },
    {  student_id: 1002, book_id: 3000 },
    {  student_id: 1002, book_id: 3001 },
    {  student_id: 1002, book_id: 3006 },
    {  student_id: 1003, book_id: 3000 },
    {  student_id: 1003, book_id: 3001 },
    {  student_id: 1003, book_id: 3004 },
    {  student_id: 1004, book_id: 3006 },
    {  student_id: 1004, book_id: 3007 },
    {  student_id: 2000, book_id: 3005 },
    {  student_id: 2000, book_id: 3004 },
    {  student_id: 2000, book_id: 3007 },
    {  student_id: 2001, book_id: 3005 },
    {  student_id: 2001, book_id: 3004 },
    {  student_id: 2001, book_id: 3007 }
];

const default_friend_map = [
    {  student_id: 1000, friend_id: 1002 },
    {  student_id: 1000, friend_id: 1003 },
    {  student_id: 1000, friend_id: 2000 },
    {  student_id: 1000, friend_id: 2001 },

    {  student_id: 1001, friend_id: 1004 },

    {  student_id: 1002, friend_id: 1000 },
    {  student_id: 1002, friend_id: 1003 },
    {  student_id: 1002, friend_id: 2001 },

    {  student_id: 1003, friend_id: 1000 },
    {  student_id: 1003, friend_id: 1002 },
    {  student_id: 1003, friend_id: 2000 },
    {  student_id: 1003, friend_id: 2001 },

    {  student_id: 1004, friend_id: 1001 },

    {  student_id: 2000, friend_id: 1000 },
    {  student_id: 2000, friend_id: 1002 },
    {  student_id: 2000, friend_id: 1003 },
    {  student_id: 2000, friend_id: 2001 },

    {  student_id: 2001, friend_id: 1000 },
    {  student_id: 2001, friend_id: 1002 },
    {  student_id: 2001, friend_id: 1003 }
]


