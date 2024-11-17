create schema if not exists bookstore;
create schema if not exists bookstore_jpa;

create table if not exists bookstore.author
(
    id  bigint not null
        constraint author_pkey primary key,
    name varchar(255) not null,
    profile jsonb
);
alter table bookstore.author owner to hql_demo;

create table if not exists bookstore.student
(
    id  bigint not null
        constraint student_pkey primary key,
    name varchar(255) not null,
    height real,
    mass real,
    metadata jsonb
);
alter table bookstore.student owner to hql_demo;

--------------------------------------------------------
create table if not exists bookstore.student_friend_link
(
    student_id bigint not null
        constraint fk_student_id_2_pk_student__id
            references bookstore.student,
    friend_id bigint not null
        constraint fk_friend_id_2_pk_student__id
            references bookstore.student
);
alter table bookstore.student_friend_link owner to hql_demo;
create unique index if not exists student_id__friend_id__uindex
    on bookstore.student_friend_link (student_id, friend_id);


--------------------------------------------------------
create table if not exists bookstore.episode
(
    title varchar(255) not null
        constraint episode_pkey
            primary key,
    published timestamp
);
alter table bookstore.episode owner to hql_demo;

--------------------------------------------------------
create table if not exists bookstore.student_episode_link
(
    student_id bigint not null
        constraint fk_student_id_2_pk_student__id
            references bookstore.student,
    episode_id varchar(255) not null
        constraint fk_episode_id_2_pk_episode__title
            references bookstore.episode
);
alter table bookstore.student_episode_link owner to hql_demo;
create unique index if not exists student_id__episode_id__uindex
    on bookstore.student_episode_link (student_id, episode_id);


--------------------------------------------------------
create table if not exists bookstore.book
(
    id  bigint not null
        constraint book_pkey
            primary key,
    title varchar(255) not null,
    author_id bigint
        constraint fk_author_id_2_pk_author__id
            references bookstore.author,
    price real
);
alter table bookstore.book owner to hql_demo;

--------------------------------------------------------

--------------------------------------------------------
create table if not exists bookstore.book_order
(
    student_id bigint not null
        constraint fk_student_id_2_pk_student__id
            references bookstore.student,
    book_id bigint not null
        constraint fk_book_id_2_pk_book__id
            references bookstore.book
);
alter table bookstore.book_order owner to hql_demo;
create unique index if not exists student_id__book_id__uindex
    on bookstore.book_order (student_id, book_id);
