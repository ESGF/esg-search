

ESGF Earth Sciences Metadata
============================

ESGF Earth Sciences metadata is intended to support accurate and
powerful searches across distributed Earth Sciences data collections.
Metadata fields are defined in the ESGF meta-schema file geo.xml.
Although this schema is always used during validation, all metadata
fields contained within are optional, so it doesn’t really preclude
records from being ingested (unless their values are malformed).

Optional Earth Science Generic Fields
-------------------------------------

-  variable (string): short variable name

   -  Example: variable = ta

-  variable_long_name (string): longer variable name for human
   consumption

   -  Example: variable_long_name = Air Temperature

-  variable_units (string): variable units

   -  Example: variable_units = K

-  cf_standard_name (string chosen from Controlled Vocabulary): term
   from the CF (Climate and Forecast) CV

   -  Example: cf_standard_name = air_temperature

Optional Fields used for Geospatial and Temporal Searches
---------------------------------------------------------

-  datetime_start (date): start of data time coverage

   -  Note: must have the format: yyyy-MM-dd’T’HH:mm:ss’Z’
   -  Example: datetime_start = 2090-01-01T12:00:00Z

-  datetime_stop (date): end of data time coverage

   -  Note: must have the format: yyyy-MM-dd’T’HH:mm:ss’Z’
   -  Example: datetime_stop = 2229-12-31T12:00:00Z

-  north_degrees (float)

   -  Note: must be -90 <= north_degrees <=90
   -  Example: north_degrees = 89.25

-  east_degrees (float)

   -  Note: must be -180 <= east_degrees <=360
   -  Example: east_degrees = 358

-  south_degrees (float)

   -  Note: must be -90 <= south_degrees <=90
   -  Example: south_degrees = -89.25

-  west_degrees (float)

   -  Note: must be -180 <= west_degrees <=360
   -  Example: west degrees = 0.0

-  height_bottom (float)

   -  Example: height_bottom = 100000.0

-  height_top (float)

   -  Example: height_top = 1000.0

-  height_units (string)

   -  Example: height_units = Pa

