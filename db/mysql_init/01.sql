create schema if not exists bookstore;
create schema if not exists bookstore_jpa;
create schema if not exists hql_demo;

GRANT ALL PRIVILEGES ON hql_demo.* TO 'hql_demo'@'%';
GRANT ALL PRIVILEGES ON bookstore.* TO 'hql_demo'@'%';
GRANT ALL PRIVILEGES ON bookstore_jpa.* TO 'hql_demo'@'%';
