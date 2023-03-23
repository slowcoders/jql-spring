create schema if not exists starwars;
create schema if not exists starwars_jpa;

GRANT ALL PRIVILEGES ON starwars.* TO 'jql_demo'@'%' IDENTIFIED BY 'jql_demo';
GRANT ALL PRIVILEGES ON starwars_jpa.* TO 'jql_demo'@'%' IDENTIFIED BY 'jql_demo';
