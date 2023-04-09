#!/usr/bin/env bash

DIR=`dirname $0`
mkdir $DIR/data/es

docker-compose -f $DIR/docker-compose.yml up es

# reset password
docker-compose exec es -it /usr/share/elasticsearch/bin/elasticsearch-reset-password

# Copy certification
# docker-compose cp es:/usr/share/elasticsearch/config/certs/http_ca.crt $DIR/data/es

# test certification
# curl --cacert data/es/http_ca.crt -u elastic:hql_demo https://localhost:9200