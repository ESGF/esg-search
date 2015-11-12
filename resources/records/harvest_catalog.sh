#!/bin/sh
# Example script to harvest a THREDDS catalog via a PULL request to teh ESGF Publishing Services
wget --no-check-certificate --ca-certificate ~/.esg/credentials.pem\
     --certificate ~/.esg/credentials.pem --private-key ~/.esg/credentials.pem\
     --verbose\
     --post-data="uri=http://aims3.llnl.gov/thredds/catalog/esgcet/1/cmip5.output1.NIMR-KMA.HadGEM2-AO.historical.mon.atmos.Amon.r1i1p1.v20130815.xml&metadataRepositoryType=THREDDS&schema=cmip5"\
     https://esgf-dev.jpl.nasa.gov/esg-search/ws/harvest
