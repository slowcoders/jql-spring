
create table if not exists bookstore.`customer`
(
    id       int auto_increment
        primary key,
    height   float                        null,
    mass     float                        null,
    metadata longtext collate utf8mb4_bin null
        check (json_valid(`metadata`)),
    name     varchar(255)                 not null,
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

create table if not exists bookstore.episode
(
    title     varchar(255) not null
        primary key,
    published datetime(6)  null
);

create table if not exists bookstore.customer_episode_link
(
    episode_id   varchar(255) not null,
    customer_id int          not null,
    constraint customer_id__episode_id__uindex
        unique (customer_id, episode_id),
    constraint fk_episode_id_2_pk_episode__title
        foreign key (episode_id) references bookstore.episode (title),
    constraint fk_customer_id_2_pk_customer__id2
        foreign key (customer_id) references bookstore.`customer` (id)
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

