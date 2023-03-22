#!/usr/bin/env bash

DIR=`dirname $0`
URL=$1

echo curl --cacert $DIR/data/es/http_ca.crt -u elastic:jql_demo \
       -X POST https://localhost:9200/$URL \
       -H "Content-Type: application/json" \
       -d \'$2\' \


curl --cacert $DIR/data/es/http_ca.crt -u elastic:jql_demo \
  -X PUT 'https://localhost:9200/$URL' \
  -H 'Content-Type: application/json' \
  -d '$2'



