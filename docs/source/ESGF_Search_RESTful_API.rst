
The ESGF Search RESTful API
===========================

The ESGF search service exposes a RESTful URL that can be used by
clients (browsers and desktop clients) to query the contents of the
underlying search index, and return results matching the given
constraints. Because of the distributed capabilities of the ESGF search,
the URL at any Index Node can be used to query that Node only, or all
Nodes in the ESGF system.

Syntax
------

The general syntax of the ESGF search service URL is:

.. code:: console

   http://<index-node>/esg-search/search?[keyword parameters as (name, value) pairs][facet parameters as (name,value) pairs]

where “” is the base URL of the search service at a given Index Node.

All parameters (keyword and facet) are optional. Also, the value of all
parameters must be URL-encoded, so that the complete search URL is well
formed.

Keywords
--------

Keyword parameters are query parameters that have reserved names, and
are interpreted by the search service to control the fundamental nature
of a search request: where to issue the request to, how many results to
return, etc.

The following keywords are currently used by the system - see later for
usage examples:

-  facets= to return facet values and counts
-  offset= , limit= to paginate through the available results (default:
   offset=0, limit=10)
-  fields= to return only specific metadata fields for each matching
   result (default: fields=*)
-  format= to specify the response document output format
-  type= (searches record of the specified type: Dataset, File or
   Aggregation)
-  replica=false/true (searches for all records, or records that are NOT
   replicas)
-  latest=true/false (searches for just the latest version, or all
   versions)
-  distrib=true/false (searches across all nodes, or the target node
   only)
-  shards= (searches the specified shards only)
-  bbox=[west, south, east, north] (searches within a geo-spatial box)
-  start=, end= (select records based on their nominal data coverage,
   i.e. their datetime_start, datetime_stop values )
-  from=, to= (select records based on when the data was marked as last
   modified, i.e. their nominal “timestamp” value)

Default Query
-------------

If no parameters at all are specified, the search service will execute a
query using all the default values, specifically:

-  query=\* (query all records)
-  distrib=true (execute a distributed search)
-  type=Dataset (return results of type “Dataset”)

Example:

-  http://esgf-node.llnl.gov/esg-search/search

Free Text Queries
-----------------

The keyword parameter query= can be specified to execute a query that
matches the given text \_ anywhere \_ in the records metadata fields.
The parameter value can be any expression following the Apache Lucene
query syntax (because it is passed “as-is” to the back-end Solr query),
and must be URL- encoded. When using the CoG user interface at any ESGF
node and project, the “query=” parameter value must be entered in the
text field at the top of the page.

Examples:

-  Search for any text, anywhere:
   http://esgf-node.llnl.gov/esg-search/search?query=\* (the default
   value of the query parameter)
-  Search for “humidity” in all metadata fields:
   http://esgf-node.llnl.gov/esg-search/search?query=humidity
-  Search for the exact sentence “specific humidity” in all metadata
   fields (the sentence must be surrounded by quotes and URL-encoded):
   http://esgf-node.llnl.gov/esg-search/search?query=%22specific%20humidity%22
-  Search for both words “specific” and “humidity”, but not necessarily
   in an exact sequence (must use a space between the two words = this
   is the same as executing a query with the logical OR):
   http://esgf-node.llnl.gov/esg-search/search?query=specific%20humidity
-  Search for the word “observations” ONLY in the metadata field
   “product” :
   http://esgf-node.llnl.gov/esg-search/search?query=product:observations
-  Using logical AND:
   http://esgf-node.llnl.gov/esg-search/search?query=airs%20AND%20humidity
   (must use upper case “AND”)
-  Using logical OR:
   http://esgf-node.llnl.gov/esg-search/search?query=airs%20OR%20humidity
   (must use upper case “OR”). This is the same as using simply a blank
   space:
   http://esgf-node.llnl.gov/esg-search/search?query=airs%20humidity
   )
-  Search for a dataset with a specific id:
   http://esgf-node.llnl.gov/esg-search/search?query=id:obs4MIPs.NASA-JPL.AIRS.hus.mon.v20110608|esgf-data.llnl.gov
-  Search for all datasets that match an id pattern:
   http://esgf-node.llnl.gov/esg-search/search?query=id:obs4MIPs.NASA-JPL.AIRS.\*

Facet Queries
-------------

A request to the search service can be constrained to return only those
records that match specific values for one or more facets. Specifically,
a facet constraint is expressed through the general form: =, where is
chosen from the controlled vocabulary of facet names configured at each
site, and must match exactly one of the possible values for that
particular facet.

When specifying more than one facet constraint in the request, multiple
values for the same facet are combined with a logical OR, while multiple
values for different facets are combined with a logical AND. Also,
multiple possible values for teh same facets can be expressed as a
comma-separated list. For example:

-  experiment=decadal2000&variable=hus : will return all records that
   match experiment=decadal2000 AND variable=hus
-  variable=hus&variable=ta : will return all records that match
   variable=hus OR variable=ta
-  variable=hus,ta : will also return all records that match
   variable=hus OR variable=ta

A facet constraint can be negated by using the != operator. For example,
model!=CCSM searches for all items that do NOT match the CCSM model.
Note that all negative facets are combined in logical AND, for example,
model!=CCSM&model!=HadCAM searches for all items that do not match CCSM,
and do not match HadCAM.

By default, no facet counts are returned in the output document. Facet
counts must be explicitly requested by specifying the facet names
individually (for example: facets=experiment,model) or via the special
notation facets=*. The facets list must be comma-separated, and white
spaces are ignored.

If facet counts is requested, facet values are sorted alphabetically
(facet.sort=lex), and all facet values are returned (facet.limit=-1),
provided they match one or more records (facet.mincount=1)

The “type” facet must be always specified as part of any request to the
ESGF search services, so that the appropriate records can be searched
and returned. If not specified explicitly, the default value is
type=Dataset .

Examples:

-  Single facet query:
   http://esgf-node.llnl.gov/esg-search/search?cf_standard_name=air_temperature
-  Query with two different facet constraints:
   http://esgf-node.llnl.gov/esg-search/search?cf_standard_name=air_temperature&project=obs4MIPs
-  Combining two values of the same facet with a logical OR:
   http://esgf-node.llnl.gov/esg-search/search?project=obs4MIPs&variable=hus&variable=ta
   (search for all obs4MIPs files that have variable “ta” OR variable
   “hus”)
-  Using a negative facet:

   -  http://esgf-node.llnl.gov/esg-search/search?project=obs4MIPs&variable=hus&variable=ta&model!=Obs-AIRS
      (search for all obs4MIPs datasets that have variable ta OR hus,
      excluding those produced by AIRS)
   -  http://esgf-node.llnl.gov/esg-search/search?project=obs4MIPs&variable!=ta&variable!=huss
      (search for all obs4MIPs datasets that do not contain neither
      variable ta nor variable huss)

-  Search a file by its tracking id:
   http://esgf-node.llnl.gov/esg-search/search?type=File&tracking_id=2209a0d0-9b77-4ecb-b2ab-b7ae412e7a3f
-  Search a file by its checksum:
   http://esgf-node.llnl.gov/esg-search/search?type=File&checksum=83df8ae93e85e26df797d5f770449470987a4ecd8f2d405159995b5cac9a410c
-  Issue a query for all supported facets and their values at one site,
   while returning no results (note that only facets with one or more
   values are returned):
   http://esgf-node.llnl.gov/esg-search/search?facets=*&limit=0&distrib=false

Facet Listings
--------------

The available facet names and values for searching data within a
specific project can be listed with a query of the form
…project=&facets=*&limit=0 (i.e. return no results). Only facet values
that match one or more records will be returned.

Examples:

-  List all obs4MIPs facet names and values:
   http://esgf-node.llnl.gov/esg-search/search?project=obs4MIPs&facets=*&limit=0
-  List all CMIP5 facet names and values:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&facets=*&limit=0

The same query with no project constraint will return all facet names
and values for ALL data across the federation:

-  List ALL facet names and values:
   http://esgf-node.llnl.gov/esg-search/search?facets=*&limit=0

To retrieve a listing of available values for only a few facets, simply
specify a comma-separated list of facet names:

-  List all values of model, experiment and project throughout the
   federation:
   http://esgf-node.llnl.gov/esg-search/search?facets=model,experiment,project&limit=0
-  List all values of model, experiment for CMIP5 data:
   http://esgf-node.llnl.gov/esg-search/search?facets=model,experiment&project=CMIP5&limit=0

Temporal Coverage Queries
-------------------------

The keyword parameters start= and/or end= can be used to query for data
with temporal coverage that overlaps the specified range. The parameter
values can either be date-times in the format “YYYY-MM-DDTHH:MM:SSZ”
(UTC ISO 8601 format), or special values supported by the Solr DateMath
syntax.

Examples:

-  Search for data in the past year:
   http://esgf-node.llnl.gov/esg-search/search?start=NOW-1YEAR
   (translates into the constraint datetime_stop:[NOW-1YEAR TO \*] or
   datetime_stop > NOW-1YEAR)
-  Search for data before the year 2000:
   http://esgf-node.llnl.gov/esg-search/search?end=2000-01-01T00:00:00Z
   (translates into the constraint datetime_start:[\* TO
   2000-01-01T00:00:00Z] or datetime_start < 2000-01-01)

Spatial Coverage Queries
------------------------

The keyword parameter bbox=[west, south, east, north] can be used to
query for data with spatial coverage that overlaps the given bounding
box. As usual, the parameter value must be URL-encoded.

Examples:

-  http://esgf-node.llnl.gov/esg-search/search?bbox=%5B-10,-10,+10,+10%5D
   ( translates to: east_degrees:[-10 TO \*] AND north_degrees:[-10 TO
   \*] AND west_degrees:[\* TO 10] AND south_degrees:[\* TO 10] )

Please note though that NOT all ESGF records contain geo-spatial
information, and therefore will not be returned by a geo-spatial search.

Distributed Queries
-------------------

The keyword parameter distrib= can be used to control whether the query
is executed versus the local Index Node only, or distributed to all
other Nodes in the federation. If not specified, the default value
distrib=true is assumed.

Examples:

-  Search for all datasets in the federation:
   http://esgf-node.llnl.gov/esg-search/search?distrib=true
-  Search for all datasets at one Node only:
   http://esgf-node.llnl.gov/esg-search/search?distrib=false

Shard Queries
-------------

By default, a distributed query (distrib=true) targets all ESGF Nodes in
the current peer group, i.e. all nodes that are listed in the local
configuration file /esg/config/esgf_shards.xml , which is continuously
updated by the local node manager to reflect the latest state of the
federation. It is possible to execute a distributed search that targets
only one or more specific nodes, by specifying them in the “shards”
parameter, as such: shards=hostname1:port1/solr,hostname2:port2/solr,….
. Note that the explicit shards value is ignored if distrib=false (but
distrib=true by default if not otherwise specified).

Examples:

-  Query for CMIP5 data at the PCMDI and CEDA sites only:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&shards=pcmdi.llnl.gov/solr,esgf-index1.ceda.ac.uk/solr
-  Query for all files belonging to a given dataset at one site only:
   http://esgf-node.llnl.gov/esg-search/search?type=File&shards=esgf-node.llnl.gov/solr&dataset_id=obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608|esgf-data.llnl.gov

Replica Queries
---------------

Replicas (Datasets and Files) are distinguished from the original record
(a.k.a. the “master”) in the Solr index by the value of two special
keywords:

-  replica: a flag that is set to false for master records, true for
   replica records.
-  master_id: a string that is identical for the master and all replicas
   of a given logical record (Dataset or File).

By default, a query returns all records (masters and replicas) matching
the search criteria, i.e. no replica=… constraint is used. To return
only master records, use replica=false, to return only replicas, use
replica=true. To search for all identical Datasets or Files (i.e. for
the master AND replicas of a Dataset or File), use master_id=….

Examples:

-  Search for all datasets in the system (masters and replicas):
   http://esgf-node.llnl.gov/esg-search/search
-  Search for just master datasets, no replicas:
   http://esgf-node.llnl.gov/esg-search/search?replica=false
-  Search for just replica datasets, no masters:
   http://esgf-node.llnl.gov/esg-search/search?replica=true
-  Search for the master AND replicas of a given dataset:
   http://esgf-node.llnl.gov/esg-search/search?master_id=cmip5.output1.LASG-CESS.FGOALS-g2.midHolocene.3hr.land.3hr.r1i1p1
-  Search for the master and replicas of a given file:
   http://esgf-node.llnl.gov/esg-search/search?type=File&master_id=cmip5.output1.MIROC.MIROC5.decadal1978.mon.ocean.Omon.r4i1p1.wfo_Omon_MIROC5_decadal1978_r4i1p1_197901-198812.nc

Latest and Version Queries
--------------------------

By default, a query to the ESGF search services will return all versions
of the matching records (Datasets or Files). To only return the very
last, up-to-date version include latest=true . To return a specific
version, use version=… . Using latest=false will return only datasets
that were superseded by newer versions.

Examples:

-  Search for all latest CMIP5 datasets:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&latest=true
-  Search for all versions of a given dataset:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&master_id=cmip5.output1.MOHC.HadCM3.decadal1972.day.atmos.day.r10i2p1&facets=version
-  Search for a specific version of a given dataset:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&master_id=cmip5.output1.NSF-DOE-NCAR.CESM1-CAM5-1-FV2.historical.mon.atmos.Amon.r1i1p1&version=20120712

Retracted Queries
-----------------

NOTE: this feature is NOT yet released

Retracted datasets are marked by “retracted=true”, and also have the
flag “latest=false” set. Consequently, retracted datasets are
automatically NOT included in any search for the latest version data
(“latest=true”), while they are automatically included in searches the
span all versions (no “latest” constraint). To search specifically for
only retracted datasets, use the constraint “retracted=true”.

Example:

-  Search for all retracted datasets in the CMIP5 project, across all
   nodes:
   https://esgf-node.llnl.gov/esg-search/search?project=CMIP5&retracted=true

Minimum and Maximum Version Queries
-----------------------------------

NOTE: this feature is NOT yet released

The special keywords “min_version” and “max_version” can be used to
query for all records that have a version greater or equal, or less or
equal, of a given numerical value. Because often in ESGF versions are
expressed as dates of the format YYYYMMDD, it is possible to query for
all records that have a version greater/less or equal of a certain date.
The two constraints can be combined with each other to specify a version
(aka date) range, and can also be combined with other constraints.

Examples:

-  All datasets with version less than a given date:
   https://esgf-node.llnl.gov/esg-search/search?max_version=20150101
-  All Obs4MIPs datasets with version between two dates:
   http://esgf-node.llnl.gov/esg-search/search?min_version=20120101&max_version=20131231&project=obs4MIPs

Results Pagination
------------------

By default, a query to the search service will return the first 10
records matching the given constraints. The offset into the returned
results, and the total number of returned results, can be changed
through the keyword parameters limit= and offset= . The system imposes a
maximum value of limit <= 10,000.

Examples:

-  Query for 100 CMIP5 datasets in the system:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&limit=100
-  Query for the next 100 CMIP5 datasets in the system:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&limit=100&offset=100

Output Format
-------------

The keyword parameter output= can be used to request results in a
specific output format. Currently the only available options are
Solr/XML (the default) and Solr/JSON.

Examples:

-  Request results in Solr XML format:
   http://esgf-node.llnl.gov/esg-search/search?format=application%2Fsolr%2Bxml
-  Request results in Solr JSON format:
   http://esgf-node.llnl.gov/esg-search/search?format=application%2Fsolr%2Bjson

Returned Metadata Fields
------------------------

By default, all available metadata fields are returned for each result.
The keyword parameter fields= can be used to limit the number of fields
returned in the response document, for each matching result. The list
must be comma-separated, and white spaces are ignored. Use fields=\* to
return all fields (same as not specifiying it, since it is the default).
Note that the pseudo field “score” is always appended to any fields
list.

Examples:

-  Return all available metadata fields for CMIP5 datasets:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&fields=\*
-  Return only the “model” and “experiment” fields for CMIP5 datasets:
   http://esgf-node.llnl.gov/esg-search/search?project=CMIP5&fields=model,experiment

Identifiers
-----------

Each search record in the system is assigned the following identifiers
(all of type string):

-  id : universally unique for each record across the federation,
   i.e. specific to each Dataset or File, version and replica (and the
   data node storing the data). It is intended to be “opaque”, i.e. it
   should not be parsed by clients to extract any information.

   -  Dataset example:
      id=obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608|esgf-data.llnl.gov
   -  File example:
      id=obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608.tro3Stderr_TES_L3_tbd_200507-200912.nc|esgf-data.llnl.gov

-  master_id : same for all replicas and versions across the federation.
   When parsing THREDDS catalogs, it is extracted from the properties
   “dataset_id” or “file_id”.

   -  Dataset example: obs4MIPs.NASA-JPL.TES.tro3.mon (for a Dataset)
   -  File example:
      obs4MIPs.NASA-JPL.TES.tro3.mon.tro3Stderr_TES_L3_tbd_200507-200912.nc

-  instance_id : same for all replicas across federation, but specific
   to each version. When parsing THREDDS catalogs, it is extracted from
   the ID attribute of the corresponding THREDDS catalog element (for
   both Datasets and Files).

   -  Dataset example: obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608
   -  File example:
      obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608.tro3Stderr_TES_L3_tbd_200507-200912.nc

Note also that the record version is the same for all replicas of that
record, but different across versions. Examples:

-  Dataset example: version=20110608
-  File example: version=1

Access URLs
-----------

In the Solr output document returned by a search, URLs that are access
points for Datasets and Files are encoded as 3-tuple of the form
“url|mime type|service name”, where the fields are separated by the
“pipe (”\|“) character, and the”mime type" and “service name” are chosen
from the ESGF controlled vocabulary.

Example of Dataset access URLs:

-  THREDDS catalog:
   http://esgf-data.llnl.gov/thredds/catalog/esgcet/1/obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608.xml#obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608|application/xml+thredds|THREDDS
-  LAS server:
   http://esgf-node.llnl.gov/las/getUI.do?catid=0C5410C250379F2D139F978F7BF48BB9_ns_obs4MIPs.NASA-JPL.TES.tro3.mon.v20110608|application/las|LAS

Example of File access URLs:

-  HTTP download:
   http://esgf-data.llnl.gov/thredds/fileServer/esg_dataroot/obs4MIPs/observations/atmos/tro3Stderr/mon/grid/NASA-JPL/TES/v20110608/tro3Stderr_TES_L3_tbd_200507-200912.nc|application/netcdf|HTTPServer
-  GridFTP download:
   gsiftp://esgf-data.llnl.gov:2811//esg_dataroot/obs4MIPs/observations/atmos/tro3Stderr/mon/grid/NASA-JPL/TES/v20110608/tro3Stderr_TES_L3_tbd_200507-200912.nc|application/gridftp|GridFTP
-  OpenDAP download:
   http://esgf-data.llnl.gov/thredds/dodsC/esg_dataroot/obs4MIPs/observations/atmos/tro3Stderr/mon/grid/NASA-JPL/TES/v20110608/tro3Stderr_TES_L3_tbd_200507-200912.nc.html|application/opendap-html|OPENDAP
-  Globus As-A-Service download:
   globus:e3f6216e-063e-11e6-a732-22000bf2d559/esg_dataroot/obs4MIPs/observations/atmos/tro3Stderr/mon/grid/NASA-JPL/TES/v20110608/tro3Stderr_TES_L3_tbd_200507-200912.nc|Globus|Globus

Wget scripting
--------------

The same RESTful API that is used to query the ESGF search services can
also be used, with minor modifications, to generate a Wget script to
download all files matching the given constraints. Specifically, each
ESGF Index Node exposes the following URL for generating Wget scripts:

.. code:: console

   http:///wget?[keyword parameters as (name, value) pairs][facet parameters as (name,value) pairs]

where again“” is the base URL of the search service at a given Index
Node. As for searching, all parameters (keyword and facet) are optional,
and the value of all parameters must be URL-encoded, so that the
complete search URL is well formed.

The only syntax differences with respect to the search URL are:

-  The keyword parameter type= is not allowed, as the wget URL always
   assumes type=File .
-  The keyword parameter format= is not allowed, as the wget URL always
   returns a shell script as response document.
-  The keyword parameter limit= is assigned a default value of
   limit=1000 (and must still be limit < 10,000).
-  The keyword parameter download_structure= is used for defining a
   relative directory structure for the download by using the facets
   value (i.e. of Files and not Datasets).
-  The keyword parameter download_emptypath= is used to define what to
   do when download_structure is set and the facet returned has no value
   (for example, when mixing files from CMIP5 and obs4MIP and selecting
   instrument as a facet value will result in all CMIP5 files returning
   an empty value)

A typical workflow pattern consists in first identifying all datasets or
files matching some scientific criteria, then changing the request URL
from “/search?” to “/wget?” to generate the corresponding shell scripts
for bulk download of files.

Examples:

-  Download all obs4MIPs files from the JPL node with variable “hus” :
   http://esgf-node.llnl.gov/esg-search/wget?variable=hus&project=obs4MIPs&distrib=false
-  Download the files as in the previous examples, and organize them in
   a directory structure such as
   project/product/institute/time_frequency :
   http://esgf-node.llnl.gov/esg-search/wget?variable=hus&project=obs4MIPs&distrib=false&download_structure=project,product,institute,time_frequency

For more information, see also the Wget FAQ

   

   
  

 
