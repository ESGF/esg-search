
ESGF Core Metadata
==================

ESGF “core” metadata includes mandatory and optional fields that are
needed to search and download data throughout the ESGF system. These
fields are not specific to any scientific discpline, rather they are
common to any discpline that intends to leverage the ESGF
infrastructure. Core metadata fields are defined in the ESGF meta-schema
file esgf.xml, which is always used for validation whenever metadata
records are published.

When a client invokes the ESGF “Pull” Publishing Services, valid
metadata records are automatically created on the server by harvesting
pre-existing THREDDS catalogs. Viceversa, when a client invokes the ESGF
“Push” Publishing Services, it is itself responsible for creating valid
metadata records before they are sent to the server for ingestion.

Required Fields
---------------

-  id (string): record identifier, must be globally unique. It is
   specific to a verion and replica of that object

   -  Note: when parsing THREDDS catalogs, it is built as
      “instance_id|data_node”
   -  Note: could be any string, including a UUID
   -  Example (for Dataset): id
      cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323|pcmdi9.llnl.gov
   -  Example (for File): id =
      cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323.huss_day_inmcm4_1pctCO2_r1i1p1_20900101-20991231.nc|pcmdi9.llnl.gov

-  title (string): record title, displayed as main text in search
   results

   -  Example: title = “project=CMIP5 / IPCC Fifth Assessment Report,
      model=Institute for Numerical Mathematics, experiment=1 percent
      per year CO2, time_frequency=day, modeling realm=atmos,
      ensemble=r1i1p1, version=20110323”

-  type (string chosen from Controlled Vocabulary): record type, used to
   enable searching on different targets

   -  Note: currently valid values are: “Dataset”, “File”, “Aggregation”
   -  Note: record of different types are mapped to separate Solr cores,
      cannot search acrosss cores
   -  Example: type=Dataset

-  project (string): provides a scientific context for these data - for
   Datasets only

   -  Example: project = CMIP5

-  dataset_id (string) - the enclosing dataset identifier, for files and
   aggregations only

   -  Note: allows to search for datasets first, files later
   -  Example: dataset_id =
      cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323|pcmdi9.llnl.gov

-  index_node (string): host indexing the data

   -  Note: allows to target the files search to a specific index node
      which greatly improves performance
   -  Note: not needed if publishing Datasets that have no files, only
      “service” level endpoints
   -  Example: index_node = pcmdi9.llnl.gov

-  data_node (string): host serving the data

   -  Note: not needed when publishing Datasets that have no files
   -  Example: data_node = pcmdi11.llnl.gov

Optional Fields needed for Versioning and Replication
-----------------------------------------------------

-  version (integer, default=0): version of a Dataset, File or
   Aggregation, if provided it must be an integer
-  master_id (string): globally unique identifier for a “logical”
   Dataset or File: it is the same across all versions and replicas of
   that object

   -  Note: allows searching for all versions and copies of the same
      logical record
   -  Example (for Dataset): master_id =
      cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1
   -  Example (for File): master_id =
      cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.huss_day_inmcm4_1pctCO2_r1i1p1_20900101-20991231.nc

      -  instance_id (string): globally unique identifier for a specific
         version of a Dataset or File, it’s the same for all replicas of
         that object, but different for different versions

   -  Note: allows searching for all replicas of the same record of a
      given version
   -  Example (for Dataset): iknstance_id =
      cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323
   -  Example (for File): instance_id
      =cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323.huss_day_inmcm4_1pctCO2_r1i1p1_20900101-20991231.nc

-  replica (boolean, default=false): enables support for logical copies
   of the same record
-  latest (boolean, default=true): enables support for searching on
   latest version of each record
-  shard (string): enables publishing to local shard to avoid
   replicating these data across the federation

   -  Example: shard=localhost:8982

-  checksum: file checksum - Files only

   -  Example: checksum = 7fcd959a4bb57e4079c8e65a7a5d0499

-  checksum_type (string from CV): the algorithm used to compute the
   checksum

   -  Example: checksum_type = SHA256

Optional Generic Fields
-----------------------

-  description (string) - additional text displayed in search results

   -  Example: description = inmcm4 model output prepared for CMIP5 1
      percent per year CO2

-  url (string, zero, one or more): record access point, encoded as
   3-tuple of the form (URL|mime-type|service name)

   -  Example: url =
      http://pcmdi9.llnl.gov/thredds/esgcet/1/cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323.xml#cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323|application/xml+thredds|Catalog
   -  Example: url =
      http://pcmdi9.llnl.gov/las/getUI.do?catid=D9C519D5A310E197819B7197215FD574_ns_cmip5.output1.INM.inmcm4.1pctCO2.day.atmos.day.r1i1p1.v20110323|application/las|LAS

-  access (string, zero, one or more): name of the service through which
   the data can be acceesed

   -  Example: access = THREDDS, access = LAS

-  timestamp (date): date the document was created or last modified - if
   such information is found

   -  Note: if provided, it must be in the form:
      yyyy-MM-dd’T’HH:mm:ss’Z’
   -  Example: timestamp = 2012-01-13T01:34:15Z

-  schema (string, must be chosen from Controlled Vocabulary) - used to
   enable server-side validation

   -  Note: must be encoded as attribute of the top-level root XML
      element
   -  Example: schema = “cmip5”

-  format (string): the data format

   -  Example: format = NetCDF

Optional Fields needed for Files Download
-----------------------------------------

-  size (long) - total size in bytes of record content (i.e. size of
   single file, or global size of all files in a dataset)
-  number_of_files (integer) - for datasets only
-  numbr_of_aggregations (integer) - for datasets only

Examples
--------

-  esgf_dataset.xml : example Dataset metadata record complying to the
   ESGF core and Earth Science schemas
-  esgf_file.xml : example File metadata record complying to the ESGF
   core and Earth Science schemas
