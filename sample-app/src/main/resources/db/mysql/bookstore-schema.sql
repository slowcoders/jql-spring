-- create schema if not exists bookstore;
-- create schema if not exists bookstore_jpa;
-- create schema if not exists hql_demo;
--
-- GRANT ALL PRIVILEGES ON hql_demo.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
-- GRANT ALL PRIVILEGES ON bookstore.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';
-- GRANT ALL PRIVILEGES ON bookstore_jpa.* TO 'hql_demo'@'%' IDENTIFIED BY 'hql_demo';

create table if not exists bookstore.`student`
(
    id       int auto_increment
    primary key,
    height   float        null,
    mass     float        null,
    metadata json         null,
    name     varchar(255) not null,
);


create table if not exists bookstore.student_friend_link
(
    student_id int not null,
    friend_id    int not null,
    constraint student_id__friend_id__uindex
        unique (student_id, friend_id),
    constraint fk_student_id_2_pk_student__id
        foreign key (student_id) references bookstore.`student` (id),
    constraint fk_friend_id_2_pk_student__id
        foreign key (friend_id) references bookstore.`student` (id)
);

create table if not exists bookstore.episode
(
    title     varchar(255) not null
        primary key,
    published datetime(6)  null
);

create table if not exists bookstore.student_episode_link
(
    episode_id   varchar(255) not null,
    student_id int          not null,
    constraint student_id__episode_id__uindex
        unique (student_id, episode_id),
    constraint fk_episode_id_2_pk_episode__title
        foreign key (episode_id) references bookstore.episode (title),
    constraint fk_student_id_2_pk_student__id2
        foreign key (student_id) references bookstore.`student` (id)
);

create table if not exists bookstore.book
(
    id       int auto_increment
        primary key,
    length   float        null,
    name     varchar(255) not null,
    student_id int          null,
    constraint fk_student_id_2_pk_student__id
        foreign key (student_id) references bookstore.`student` (id)
);

