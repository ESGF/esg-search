#!/bin/sh
# example script to PUSH an XML record to the ESGF Publishing Services
wget --no-check-certificate --ca-certificate ~/.esg/credentials.pem\
     --certificate ~/.esg/credentials.pem --private-key ~/.esg/credentials.pem\
     --verbose --post-file=./esgf_dataset.xml\
     https://esgf-dev.jpl.nasa.gov/esg-search/ws/publish
