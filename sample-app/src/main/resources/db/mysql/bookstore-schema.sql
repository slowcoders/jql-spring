-- create schema if not exists bookstore;
-- create schema if not exists bookstore_jpa;
-- create schema if not exists hql_demo;
--
-- GRANT ALL PRIVILEGES ON hql_demo.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
-- GRANT ALL PRIVILEGES ON bookstore.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
-- GRANT ALL PRIVILEGES ON bookstore_jpa.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';

create table if not exists bookstore.`customer`
(
    id       int auto_increment
    primary key,
    height   float        null,
    mass     float        null,
    metadata json         null,
    name     varchar(255) not null,
);


create table if not exists bookstore.customer_friend_link
(
    customer_id int not null,
    friend_id    int not null,
    constraint customer_id__friend_id__uindex
        unique (customer_id, friend_id),
    constraint fk_customer_id_2_pk_customer__id
        foreign key (customer_id) references bookstore.`customer` (id),
    constraint fk_friend_id_2_pk_customer__id
        foreign key (friend_id) references bookstore.`customer` (id)
);

create table if not exists bookstore.book
(
    id       int auto_increment
        primary key,
    length   float        null,
    name     varchar(255) not null,
    customer_id int          null,
    constraint fk_customer_id_2_pk_customer__id
        foreign key (customer_id) references bookstore.`customer` (id)
);

