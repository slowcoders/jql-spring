-- create schema if not exists starwars;
-- create schema if not exists starwars_jpa;
-- create schema if not exists hql_demo;
--
-- GRANT ALL PRIVILEGES ON hql_demo.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
-- GRANT ALL PRIVILEGES ON starwars.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
-- GRANT ALL PRIVILEGES ON starwars_jpa.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';

create table if not exists starwars.`author`
(
    id       int auto_increment
    primary key,
    height   float        null,
    mass     float        null,
    metadata json         null,
    name     varchar(255) not null,
    species  varchar(255) not null
);


create table if not exists starwars.author_friend_link
(
    author_id int not null,
    friend_id    int not null,
    constraint author_id__friend_id__uindex
        unique (author_id, friend_id),
    constraint fk_author_id_2_pk_author__id
        foreign key (author_id) references starwars.`author` (id),
    constraint fk_friend_id_2_pk_author__id
        foreign key (friend_id) references starwars.`author` (id)
);

create table if not exists starwars.episode
(
    title     varchar(255) not null
        primary key,
    published datetime(6)  null
);

create table if not exists starwars.author_episode_link
(
    episode_id   varchar(255) not null,
    author_id int          not null,
    constraint author_id__episode_id__uindex
        unique (author_id, episode_id),
    constraint fk_episode_id_2_pk_episode__title
        foreign key (episode_id) references starwars.episode (title),
    constraint fk_author_id_2_pk_author__id2
        foreign key (author_id) references starwars.`author` (id)
);

create table if not exists starwars.book
(
    id       int auto_increment
        primary key,
    length   float        null,
    name     varchar(255) not null,
    author_id int          null,
    constraint fk_author_id_2_pk_author__id
        foreign key (author_id) references starwars.`author` (id)
);

