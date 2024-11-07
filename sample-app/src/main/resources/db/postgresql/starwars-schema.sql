create schema if not exists starwars;
create schema if not exists starwars_jpa;

create table if not exists starwars.author
(
    id  bigint not null
        constraint author_pkey primary key,
    species varchar(31) not null,
    name varchar(255) not null,
    height real,
    mass real,
    metadata jsonb
);
alter table starwars.author owner to hql_demo;

--------------------------------------------------------
create table if not exists starwars.author_friend_link
(
    author_id bigint not null
        constraint fk_author_id_2_pk_author__id
            references starwars.author,
    friend_id bigint not null
        constraint fk_friend_id_2_pk_author__id
            references starwars.author
);
alter table starwars.author_friend_link owner to hql_demo;
create unique index if not exists author_id__friend_id__uindex
    on starwars.author_friend_link (author_id, friend_id);


--------------------------------------------------------
create table if not exists starwars.episode
(
    title varchar(255) not null
        constraint episode_pkey
            primary key,
    published timestamp
);
alter table starwars.episode owner to hql_demo;

--------------------------------------------------------
create table if not exists starwars.author_episode_link
(
    author_id bigint not null
        constraint fk_author_id_2_pk_author__id
            references starwars.author,
    episode_id varchar(255) not null
        constraint fk_episode_id_2_pk_episode__title
            references starwars.episode
);
alter table starwars.author_episode_link owner to hql_demo;
create unique index if not exists author_id__episode_id__uindex
    on starwars.author_episode_link (author_id, episode_id);


--------------------------------------------------------
create table if not exists starwars.book
(
    id  bigint not null
        constraint book_pkey
            primary key,
    author_id bigint
        constraint fk_author_id_2_pk_author__id
            references starwars.author,
    length real,
    name varchar(255) not null
);
alter table starwars.book owner to hql_demo;

--------------------------------------------------------

