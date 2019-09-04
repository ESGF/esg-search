

Metadata Validation
===================

Schema HIrearchy
----------------

The ESGF Publishing Services enforce record validation: before being
ingested into the metadata index, all incoming records are validated for
basic compliance requirements, and optionally for project-specific
compliance.

Record validation is based on meta-schemas - XML document instances that
encode the rules to be applied by the validation engine. Currently,
meta- schemas are distributed as part of the “esgf-search” module in the
ESGF GitHub repository, in the future they might be made available and
read from some URL location. Meta-schemas are modularized to encode
distinct sets of requirements. Specifically at this time the following
schemas are available:

-  esgf.xml : contains core requirements that ALL XML records must
   comply with (each record must have an “id”, a “type”, a “title”,
   etc.). This schema is always enforced.

-  geo.xml : contains requirements for Earth Sciences data. This schema
   is always enforced, but all of its elements are optional so it only
   applies to Earth Sciences datasets.

-  cmip5.xml : contains requirements specific to CMIP5 model data (and
   similar datasets such as obs4MIPs and ana4MIPs). This schema is
   enforced only if the publisher agent explicitly requests it (in
   “pull” operations), or flags the records as such (in “push”
   operations).

Solr compliance
---------------

Solr uses its own schema definition document (schema.xml inside each
core conf/ directory) to validate the incoming metadata records.
Unfortunately, this document lacks all the information that is needed
for ESGF validation, and therefore ESGF metadata must be defined in two
different places.

Luckily, the definition of ESGF metadata fields within the Solr
schema.xml document may be greatly shortened by defining some naming
conventions:

-  Some fields such as id, title, type, etc. are defined explicitely
   either because they are single-valued, or because they are of type
   date, float, etc.
-  Fields that start or end in “date” are considered of type “date” and
   can only have one value
-  Otherwise, all other fields are considered multiple-values, and of
   type string

Meta-Schemas
------------

ESGF meta-schemas are XML documents (conforming to a single XSD schema)
that allow for encoding of a complex validation semantics, specifically:

-  The required and optional metadata elements and their cardinality.
-  The metadata element type, including advanced or custom types such as
   “uuid”. The default type if not specified is “string”.
-  The record types to which they apply: “Dataset”, “File”,
   “Aggregation”. By default, they apply to all record types.
-  An optional minimum value and maximum value for numeric fields.
-  An optional controlled vocabulary for string fields.

Meta-schemas are parsed by the ESGF validation engine, that enforces the
corresponding rules on the incoming XML records (after converting them
to Java objects). The validation engine may also apply higher logic to
specific fields: for example, “url” fields are inspected for being of
the form “url|mimeType|serverName”. Note that currently the ESGF
validation engine adopts a “lenient” approach: if a metadata field is
found in the incoming XML record, but not constrained by any meta-schema
requirement, it is still ingested as a multi-valued field of type
“string” (assuming it is not defined otherwise by the Solr engine own
schema, in which case the field will pass ESGF validation but may be
rejected by Solr).

As mentioned, “core” validation is enforced for all publishing
operations - both “push” and “pull”. Additional validation based on some
other schema (such as “schema=cmip5”) must be requested by the client:

-  pull publishing : the additional HTTP POST parameter “schema=….” must
   be specified
-  push publishing : the Solr/XML document that is the payload of the
   request must be flagged as conforming to the desired schema via the
   “doc@schema” XML attribute (which is ignored by the Solr engine upon
   ingestion). Note that no validation is applied to un-publishing or
   delete operations (as the only field that matters is the record
   “id”).

Appendix: XSD versus ESGF meta-schemas
--------------------------------------

Historically, the following considerations led to the decision of using
custom meta-schemas instead of standard XSD documents for validating
ESGF records:

-  Advantages of XSD documents:

   -  Validation can be performed by standard libraries available in all
      languages.
   -  XML records conforming to XSD schemas can be validated by non-ESGF
      software and engines.

-  Disadvantages of XSD schemas:

   -  XSDs are written in a very complex and verbose syntax, that
      require humans to gain specific training to understand and modify
   -  At this time, XSDs require optional metadata fields to follow a
      specific order in the document (this will change in the near
      future, but library support will take some time).

-  Advantages of ESGF meta-schemas

   -  They are much simpler than XSD schemas…
   -  …yet they allow to encode a richer semantics such as record type
      dependency, custom metadata types, etc.
   -  They can be enforced on XML documents conforming to Solr/XML, as
      opposed to documents conforming to some other XSD schema. Solr/XML
      documents are very simple, uniform across all projects, and can be
      directly ingested into Solr without the need for an additional
      conversion.
   -  The validating engine can apply any additional custom logic (for
      example, for “url” fields).

