create schema if not exists library;
create schema if not exists library_jpa;

create table if not exists library.author
(
    id  bigint not null
        constraint author_pkey primary key,
    name varchar(255) not null,
    profile jsonb
);
alter table library.author owner to hql_demo;

create table if not exists library.customer
(
    id  bigint not null
        constraint customer_pkey primary key,
    name varchar(255) not null,
    height real,
    mass real,
    metadata jsonb
);
alter table library.customer owner to hql_demo;

--------------------------------------------------------
create table if not exists library.customer_friend_link
(
    customer_id bigint not null
        constraint fk_customer_id_2_pk_customer__id
            references library.customer,
    friend_id bigint not null
        constraint fk_friend_id_2_pk_customer__id
            references library.customer
);
alter table library.customer_friend_link owner to hql_demo;
create unique index if not exists customer_id__friend_id__uindex
    on library.customer_friend_link (customer_id, friend_id);


--------------------------------------------------------
create table if not exists library.episode
(
    title varchar(255) not null
        constraint episode_pkey
            primary key,
    published timestamp
);
alter table library.episode owner to hql_demo;

--------------------------------------------------------
create table if not exists library.customer_episode_link
(
    customer_id bigint not null
        constraint fk_customer_id_2_pk_customer__id
            references library.customer,
    episode_id varchar(255) not null
        constraint fk_episode_id_2_pk_episode__title
            references library.episode
);
alter table library.customer_episode_link owner to hql_demo;
create unique index if not exists customer_id__episode_id__uindex
    on library.customer_episode_link (customer_id, episode_id);


--------------------------------------------------------
create table if not exists library.book
(
    id  bigint not null
        constraint book_pkey
            primary key,
    title varchar(255) not null,
    author_id bigint
        constraint fk_author_id_2_pk_author__id
            references library.author,
    price real
);
alter table library.book owner to hql_demo;

--------------------------------------------------------

--------------------------------------------------------
create table if not exists library.book_order
(
    customer_id bigint not null
        constraint fk_customer_id_2_pk_customer__id
            references library.customer,
    book_id bigint not null
        constraint fk_book_id_2_pk_book__id
            references library.book
);
alter table library.book_order owner to hql_demo;
create unique index if not exists customer_id__book_id__uindex
    on library.book_order (customer_id, book_id);
