create schema if not exists starwars;
create schema if not exists starwars_jpa;
create schema if not exists hql_demo;

GRANT ALL PRIVILEGES ON hql_demo.* TO 'hql_demo'@'%';
GRANT ALL PRIVILEGES ON starwars.* TO 'hql_demo'@'%';
GRANT ALL PRIVILEGES ON starwars_jpa.* TO 'hql_demo'@'%';
