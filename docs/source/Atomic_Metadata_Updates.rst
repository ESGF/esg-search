Atomic Metadata Updates
======================+

This document explains how to update metadata records that have already
been published to an ESGF Index Node (backed up by a corresponding Solr
Index). This technique allows to add/change/remove single metadata
fields to/from an existing ESGF record, without having to republish the
full updated record with the same “id” field.

Semantics
---------

A metadata update changes an existing record through the following
possible actions:

-  set: sets one or more values for the specified field, replacing any
   current values if already existing
-  add: adds one or more values to the specified field
-  remove: removes one or more values from the specified field

Additionally, all values can be removed from the specified field by
using “set” with no values specified. Note that Solr does not allow to
mix set/add/remove actions in the same request.

APIs
----

Metadata updates of ESGF records can be executed using three different
APIs:

1. The native Solr API for atomic updates

   -  The client targets the “master” Solr URL directly:
      http://:8984/solr//update. Updates will be automatically
      replicated to the “slave” Solr index on port 8983
   -  No authentication or authorization is required. The client must
      have access to port 8984 and therefore must execute from within
      the institution firewall.
   -  The client must issue a POST request with a document that follows
      the Solr syntax for atomic updates (in JSON or XML).
   -  Each request can only update one document at a time.
   -  See Solr documentation for details.

2. The ESGF Python client API for Solr updates

   -  Python client library to interact with the Solr server directly to
      request metadata updates
   -  Hides behind the scenes the creation of Solr XML document,
      encoding metadata update instructions as Python dictionaries
      instead
   -  Again no authentication or authorization needed, assuming that the
      Solr master URL is open to the Python client for updates
      (typically, from localhost)
   -  See instructions at https://github.com/ESGF/esgfpy-solr

3. The ESGF metadata update API

   -  The client targets the ESGF Search web service URLs:
      https:///esg-search/ws/update… which behind the scenes query and
      update the “master” Solr index. Updates are again automatically
      replicated to the “slave” Solr index. -The client must provide an
      X509 certificate used for authentication and authorization.
   -  Authorization is based on the individual “id”s of all records been
      updated.
   -  The client request may originate from any server on the web, since
      it will be undergo proper authorization.
   -  The ESGF API can be used for bulk-updates of many records at once
      - infact, for all records that match an ESGF search.

ESGF Atomic Metadata Updates
----------------------------

Pre-Requisites
~~~~~~~~~~~~~~

-  The client must obtain a valid X.509 certificate that is transmitted
   as part of the request
-  The certificate must contain an identity that has been authorized to
   publish to that dataset (based on the record “id” field)

GET HTTPS Requests
~~~~~~~~~~~~~~~~~~

The ESGF Search web services support metadata updates through simple GET
request, but in this case each request can only update one document. The
general syntax of a GET request is:

https:///esg-search/ws/updateById?id=…..&action=…&core=…&field=…&value=…&value=…&value=…

where:

-  id: record identifier (properly URL-encoded)
-  action: set/add/remove
-  field: the metadata field to be updated
-  value: one or more values to be set/added/removed

The following examples show how to use the popular wget client to issue
GET metadata updates requests (each example must be entered all in one
line):

#set one or more values

| wget –no-check-certificate –ca-certificate ~/.esg/credentials.pem
  –certificate ~/.esg/credentials.pem
| –private-key ~/.esg/credentials.pem –verbose
| ‘https://esgf-dev.jpl.nasa.gov/esg-search/ws/updateById?id=obs4MIPs.NASA-JPL.AIRS.mon.v1%7Cesg-datanode.jpl.nasa.gov
  &action=set&core=datasets&field=xlink&value=cnn&value=abc’

#add one or more values

| wget –no-check-certificate –ca-certificate ~/.esg/credentials.pem
  –certificate ~/.esg/credentials.pem
| –private-key ~/.esg/credentials.pem –verbose
| ‘https://esgf-dev.jpl.nasa.gov/esg-search/ws/updateById?id=obs4MIPs.NASA-JPL.AIRS.mon.v1%7Cesg-datanode.jpl.nasa.gov
  &action=add&core=datasets&field=xlink&value=cbs’

#remove one or more values

| wget –no-check-certificate –ca-certificate ~/.esg/credentials.pem
  –certificate ~/.esg/credentials.pem
| –private-key ~/.esg/credentials.pem –verbose
| ‘https://esgf-dev.jpl.nasa.gov/esg-search/ws/updateById?id=obs4MIPs.NASA-JPL.AIRS.mon.v1%7Cesg-datanode.jpl.nasa.gov
  &action=remove&core=datasets&field=xlink&value=cnn&value=abc’

#remove all values

| wget –no-check-certificate –ca-certificate ~/.esg/credentials.pem
  –certificate ~/.esg/credentials.pem
| –private-key ~/.esg/credentials.pem –verbose
| ‘https://esgf-dev.jpl.nasa.gov/esg-search/ws/updateById?id=obs4MIPs.NASA-JPL.AIRS.mon.v1%7Cesg-datanode.jpl.nasa.gov
  &action=set&core=datasets&field=xlink’

POST HTTPS Requests
~~~~~~~~~~~~~~~~~~~

POST requests to the ESGF Search Services are very powerful, because
they allow to update at once ALL records that match one or more queries
(expressed in the ESGF search syntax). In a POST request, one or more
metadata updates are specified through an XML document that conforms to
a custom ESGF syntax. For example, the following command can be used to
send a POST metadata update request using the wget client:

| wget –no-check-certificate –ca-certificate ~/.esg/credentials.pem
  –certificate ~/.esg/credentials.pem
| –private-key ~/.esg/credentials.pem –verbose –post-file=updates.xml
| ‘https://esgf-dev.jpl.nasa.gov/esg-search/ws/update’

where the XML document has the following content, depending on what kind
of metadata update is requested:

.. raw:: html

   <!-- set one or more values on a single record (identified by "id") -->

 id=obs4MIPs.NASA-JPL.AIRS.mon.v1|esgf-dev.jpl.nasa.gov abc 123456

.. raw:: html

   <!-- set one or more values on multiple records (identified by an ESGF query) -->

 project=obs4MIPs&realm=atmos xyzuvw 999999

.. raw:: html

   <!-- add one or more values (by query) -->

 project=obs4MIPs&realm=atmos qazxsw 9876543210

.. raw:: html

   <!-- remove one or more values (by query) -->

 project=obs4MIPs&realm=atmos qazxsw

.. raw:: html

   <!-- remove all values (by query) -->

 project=obs4MIPs&realm=atmos

Targetting the Local Shard
~~~~~~~~~~~~~~~~~~~~~~~~~~

All the above examples update metadata in the “master” Solr index by
targetting the two URLs “https:///esg-search/ws/update” (POST) and
“https:///esg-search/ws/updateById” (GET). To update metadata records
that were published to the Local Shard, use the following URLs:

-  POST: https:///esg-search/ws/updateLocal
-  GET: https:///esg-search/ws/updateByIdLocal

