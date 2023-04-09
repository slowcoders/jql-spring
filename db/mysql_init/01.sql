create schema if not exists starwars;
create schema if not exists starwars_jpa;
create schema if not exists hyper_jdbc;

GRANT ALL PRIVILEGES ON hyper_jdbc.* TO 'hyper_jdbc'@'%';
GRANT ALL PRIVILEGES ON starwars.* TO 'hyper_jdbc'@'%';
GRANT ALL PRIVILEGES ON starwars_jpa.* TO 'hyper_jdbc'@'%';
