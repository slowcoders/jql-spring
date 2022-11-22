docker run --rm -p 5432:5432 \
  -v `pwd`/postgres_data:/var/lib/postgresql/data \
  -e POSTGRES_USER=jql_demo \
  -e POSTGRES_PASSWORD=jql_demo \
  -e POSTGRES_DB=jql_demo \
  postgres:14.1