#!/bin/sh
# script that starts all the Solr Cloud nodes

cd $SOLR_CLOUD_HOME

solr/bin/solr start -c -p 8983 -s node8983/solr/
solr/bin/solr start -c -p 8984 -s node8984/solr/ -z localhost:9983
solr/bin/solr start -c -p 8985 -s node8985/solr/ -z localhost:9983