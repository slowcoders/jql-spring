create schema if not exists bookstore;
-- create schema if not exists bookstore_jpa;
-- create schema if not exists hql_demo;
--
-- GRANT ALL PRIVILEGES ON hql_demo.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
# GRANT ALL PRIVILEGES ON bookstore.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
-- GRANT ALL PRIVILEGES ON bookstore_jpa.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';

create table if not exists bookstore.author
(
    id      bigint       not null
        primary key,
    name    varchar(255) not null,
    profile json         null
);

create table if not exists bookstore.customer
(
    id     bigint       not null
        primary key,
    height float        null,
    mass   float        null,
    memo   json         null,
    name   varchar(255) not null
);


create table if not exists bookstore.customer_friend_link
(
    customer_id bigint not null,
    friend_id   bigint not null,
    constraint customer_id__friend_id__uindex
        unique (customer_id, friend_id),
    constraint fk_customer_friend_link__customer_id_2_pk_customer__id
        foreign key (customer_id) references bookstore.customer (id),
    constraint fk_customer_friend_link__friend_id_2_pk_customer__id
        foreign key (friend_id) references bookstore.customer (id)
);

create table if not exists bookstore.book
(
    id           bigint       not null
        primary key,
    price        float        null,
    title        varchar(255) not null,
    author_id    bigint       null,
    publisher_id bigint       null,

    constraint fk_book_author_id_2_pk_author__id
        foreign key (author_id) references bookstore.author (id)
);


create table if not exists bookstore.publisher
(
    id   bigint       not null
        primary key,
    memo json         null,
    name varchar(255) not null
);

create table if not exists bookstore.book_order
(
    customer_id bigint not null,
    book_id     bigint not null,
    primary key (customer_id, book_id),
    constraint fk_book_order__customer_id_2_pk_customer__id
        foreign key (customer_id) references bookstore.customer (id),
    constraint fk_book_order__book_id_2_pk_book__id
        foreign key (book_id) references bookstore.book (id)
);


