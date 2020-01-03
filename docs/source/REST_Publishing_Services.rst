
ESGF REST Publishing Services
=============================

The ESGF REST Publishing Services represent the “next generation” set of
services for publishing data into ESGF - as opposed to the old
Hessian-based services which are still supported, but will eventually be
phased out. The main features of the new framework are:

-  The capability to publish resources of any kind, not just files
   (mainly in NetCDF format) listed in THREDDS catalogs
-  The capability to publish resources by invoking either “push” or
   “pull” operations
-  The capability to validate metadata records upoin ingestion
-  The representation of the service endpoints as simple REST URLs

The new services are secured in exactly the same way as the old
services. Specifically, publication is controlled by local policies on
the Data Node, which match classes of resources to groups and roles
authorized for publishing. As part of the request, the publishing client
needs to transmit to the server an X509 certificate containing the
identity of the publication agent. The server uses the agent identity to
invoke the local authorization service.

API
---

The Publishing Services API consists of matching RESTful endpoints for
publishing and unpublishing metadata, in both pull and push mode.
Additionally, a service is provided to delete records by identier.

Behavior common to all services:

-  All services must be invoked by clients via HTTPS POST requests
-  The client request must include an X.509 certificate for
   authentication and authorization
-  Currently, all invocations are synchronous - i.e. the server, upon
   receiving a request, starts processing and only returns a response to
   the client when the operation is completed (succesfully or not). Note
   that this is the same behavior as the old services. Asynchronous
   invocations may be supported in a future release.
-  The response returned to the client contains:

   -  The standard HTTP status code indicating the result of the
      publishing operation. In particular, the following codes may be
      returned:

      -  200 OK: the publishing operation was succesfull
      -  400 Bad Request: request parameters are missing or have
         incorrect values
      -  401 Unauthorized: publishing operation failed because agent
         lacked the proper permission
      -  500 Internal Server Error: publishing operation failed because
         of an unspecified error arised on the server side

         -  A body encoded as XML containing a short confirmation
            message in case of success, or an error message in case of
            failure

Throughout this document, example service invocations are provided using
the popular WGET client. Additionally, at the end we also show an
example Python script that invokes a service endpoint leveraging the
Python “Requests” library.

Prerequisites
-------------

The examples below assumes the following initial setup:

-  The publishing agent has obtained an X.509 certificate from an ESGF
   trusted MyProxy server
-  The openid identity contained in the certificate is authorized to
   publish data into the target Index Node

Example on how to obtain an X.509 certificate using the myproxy-logon
client:

.. code:: console

   myproxy-logon -s esgf-node.llnl.gov -p 7512 -l -t 48 -o ~/.esg/credentials.pem

“Pull” Operations
-----------------

In “pull” mode, the client requests the server to harvest metadata from
a repository. Records are generated on the server side, validated, and
sent to the metadata store for ingestion. Note that for Pull operations
the server authorizes the user to execute the operation based on the
resource “uri”.

Pull Publishing Service
~~~~~~~~~~~~~~~~~~~~~~~

   -  URL: https://\<index-node\>/esg-search/ws/harvest
   -  HTTP POST data: encoded as form (key, value) pair

      - uri: location identifier of remote metadata repository or catalog
      - metadataRepositoryType: type of metadata repository, chosen from controlled vocabulary
      - schema: optional URI of additional schema for record validation

Example (must be entered only on one line, with … removed):

.. code:: console

   wget --no-check-certificate --ca-certificate ~/.esg/credentials.pem\
        --certificate ~/.esg/credentials.pem --private-key ~/.esg/credentials.pem\
        --verbose\
        --post-data="uri=https://esgf-node.llnl.gov/thredds/catalog/esgcet/1/...
                     ...NASA-JPL.COUND.AMSRE.LWP.mon.v1.xml&metadataRepositoryType=THREDDS"\ 
     https://esgf-dev.llnl.gov/esg-search/ws/harvest

 

Pull UnPublishing Service
~~~~~~~~~~~~~~~~~~~~~~~~~

   -  URL: https://\<index-node\>/esg-search/ws/unharvest
   -  HTTP POST data: encoded as form (key, value) pairs 

      -  uri: location identifier of remote metadata repository or catalog
      - metadataRepositoryType: type of metadata repository, chosen from controlled vocabulary

Example (must be entered only on one line, with … removed):

.. code:: console

   wget –-no-check-certificate –-ca-certificate ~/.esg/credentials.pem
         –-certificate ~/.esg/credentials.pem –-private-key  ~/.esg/credentials.pem\
        –-verbose
        –-post-data="uri=https://esgf-data1.llnl.gov/thredds/catalog/esgcet/1/…
                  …NASA-JPL.COUND.AMSRE.LWP.mon.v1.xml
        https://esgf-node.llnl.gov/esg-search/ws/unharvest

“Push” Operations
-----------------

In “push” mode, the client sends already generated metadata records to
the server. The server validates the records and send them to the
metadata store for ingestion. Client authorization is based on the “id”
of the resource that is been published or unpublished.

Push Publishing Service
~~~~~~~~~~~~~~~~~~~~~~~

-  URL: https://<index-node>/esg-search/ws/publish
-  HTTP POST data: metadata record encoded as Solr/XML (with optional
   “schema” attribute for additional project-specific validation).

Example (must be entered only on one line):

.. code:: console

   wget –-no-check-certificate –-ca-certificate ~/.esg/credentials.pem
         –-certificate ~/.esg/credentials.pem –private-key  ~/.esg/credentials.pem
         –-verbose –-post-file=cmip5_dataset.xml
          https://esgf-dev.llnl.gov/esg-search/ws/publish

The ESGF Search GitHub repository contains several examples of valid
metadata records that can be published to an ESGF Index Node:

-  esgf_dataset.xml : example Dataset metadata record complying to the
   ESGF core and Earth Science schemas
-  esgf_file.xml : example File metadata record complying to the ESGF
   core and Earth Science schemas
-  cmip5_dataset.xml : example CMIP5 Dataset metadata record
-  cmip5_file.xml : example CMIP5 File metadata record

Note that the ESGF metadata store is a Solr index, not a relational
database: therefore, no relational integrity is enforced between file
records and dataset records. The client must take care of making sure
that the file records reference an existing dataset record.

Push UnPublishing Service
~~~~~~~~~~~~~~~~~~~~~~~~~

-  URL: https://<index-node>/esg-search/ws/unpublish
-  HTTP POST data: metadata record encoded as Solr/XML (same that was
   used for publishing, although only the “id” and “type” information
   will really be used).

Example (must be entered only on one line):

.. code:: console

   wget --no-check-certificate –--ca-certificate ~/.esg/credentials.pem
         –-certificate ~/.esg/credentials.pem –-private-key  ~/.esg/credentials.pem
         –-verbose –-post-file=cmip5_dataset.xml
         https://esgf-node.llnl.gov/esg-search/ws/unpublish

Note that unpublishing a dataset record will automatically unpublish all
file and aggregation records that reference that dataset.

Delete Operations
-----------------

A generic “delete” service is provided to remove records by identifier
from the metadata store. 


Delete UnPublishing Service
~~~~~~~~~~~~~~~~~~~~~~~~~~~

-  URL: https://<index-node>/esg-search/ws/delete
-  HTTP POST data: encoded as form (key, value) pairs

   -  id: identifier of record to be deleted (key and value pairs may be
      repeated any number of times to delete more than one record at a
      time)

Example (must be entered only on one line, with … removed):

.. code:: console

   wget –-no-check-certificate –-ca-certificate ~/.esg/credentials.pem
         –-certificate ~/.esg/credentials.pem –-private-key  ~/.esg/credentials.pem
         –-verbose -O response.xml
         –-post-data=“id=cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323…
                      …|pcmdi9.llnl.gov”
         https://esgf-dev.llnl.gov/esg-search/ws/delete

Note that just like before, unpublishing a dataset record will
automatically unpublish all file and aggregation records that reference
that dataset.

Retract Operations
------------------

Datasets can be “retracted” when they are not deemed fit for use in
scientfiic research - for example because some major problem was found.
In this case, all file and aggregations records are physically deleted
from the catalog (so that data cannot be downloaded any more), but the
dataset record is kept in the catalog for reference, and marked as
“retracted”.

Retract UnPublishing Service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-  URL: https://<index-node>/esg-search/ws/retract
-  HTTP POST data: encoded as form (key, value) pairs

   -  id: identifier of record to be retracted (key and value pairs may
      be repeated any number of times to delete more than one record at
      a time)

Example (must be entered only on one line, with … removed):

.. code:: console

   wget –-no-check-certificate –ca-certificate ~/.esg/credentials.pem
        –-certificate ~/.esg/credentials.pem –-private-key  ~/.esg/credentials.pem
        –-verbose -O response.xml
        –-post-data=“id=cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323..pcmdi9.llnl.gov”
        https://esgf-dev.llnl.gov/esg-search/ws/retract

Python Client Example
---------------------

Following is an example on how to invoke the ESGF Publishing Services
from a Python client. The example leverages the Python Requests library
for HTTP(s) communication with the server.

.. code:: python

   import requests

   url = “https://esgf-dev.llnl.gov/esg-search/ws/harvest” 
   mycertpath = “/Users/cinquini/.esg/credentials.pem” 
   catalog =  “http://aims3.llnl.gov/thredds/catalog/esgcet/1/”
            +“cmip5.output1.NIMR-KMA.HadGEM2-AO.historical.mon.atmos.Amon.r1i1p1.v20130815.xml”
   postdata = {“uri” : catalog,
   “metadataRepositoryType”:“THREDDS”,
   “schema”:“cmip5” }

   resp = requests.post(url, cert=(mycertpath, mycertpath), data=postdata, verify=False ) 
   print resp.status_code 
   print resp.text

Cut-and-paste the above script into a file, for example
“client_example.py”, and execute as: python client_example.py .

REST Publishing to Local Shard
------------------------------

The ESGF REST Publishing Services support an alternative set of web
service endpoints that will publish/unpublish metadata to/from the local
Solr instance runninig on port 8982. Specifically, to target the local
shard, a client must use the following URLs:

-  https://<index-node>/esg-search/ws/harvestLocal
-  https://<index-node>/esg-search/ws/unharvestLocal
-  https://<index-node>/esg-search/ws/publishLocal
-  https://<index-node>/esg-search/ws/unpublishLocal
-  https://<index-node>/esg-search/ws/deleteLocal
-  https://<index-node>/esg-search/ws/retractLocal
