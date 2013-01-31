/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.search.query.impl.solr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import esg.search.query.api.QueryParameters;

/**
 * Class containing parameters for the Solr XML schema.
 */
public class SolrXmlPars {
	
	final public static String ELEMENT_ADD = "add";
	final public static String ELEMENT_DOC = "doc";
	final public static String ELEMENT_FIELD = "field";
	final public static String ELEMENT_LST = "lst";
	final public static String ELEMENT_RESULT = "result";
	final public static String ELEMENT_INT = "int";
	final public static String ELEMENT_STR = "str";
	final public static String ELEMENT_ARR = "arr";
	final public static String ELEMENT_DELETE = "delete";
	final public static String ELEMENT_ID = "id";
	final public static String ELEMENT_QUERY = "query";
	
	final public static String ATTRIBUTE_SCHEMA = "schema";
	
	
	final public static String  ELEMENT_FACET_COUNTS = "facet_counts";
	final public static String  ELEMENT_FACET_FIELDS = "facet_fields";
	
	final public static String ATTRIBUTE_NAME = "name";
	final public static String ATTRIBUTE_NUM_FOUND = "numFound";
	final public static String ATTRIBUTE_START = "start";
	
	final public static String ATTRIBUTE_VALUE_RESPONSE = "response";
	
	// field names: generic search record
    // <field name="id">0</field>
	// <field name="title">Record #0</field>
    // <field name="type">Dataset</field>
    // <field name="frequency">Monthly</field>
	// <field name="version">0</field>
    // .........
	//final public static String FIELD_ID = "id";
	//final public static String FIELD_PARENT_ID = "parent_id"; // note: generic notation to allow parent-child relations beyond just datasets
	//final public static String FIELD_VERSION = "version";
	//final public static String FIELD_TITLE = "title";
	//final public static String FIELD_NAME = "name";
	//final public static String FIELD_DESCRIPTION = "description";
	//final public static String FIELD_TYPE = "type";
	//final public static String FIELD_URL = "url";
	//final public static String FIELD_SERVICE = "service";
	//final public static String FIELD_SERVICE_TYPE = "service_type";
	//final public static String FIELD_XLINK = "xlink";
	//final public static String FIELD_XREF = "xref";
	//final public static String FIELD_TIMESTAMP = "timestamp";
	
	// field names: metadata file information
	final public static String FIELD_METADATA_FORMAT = "metadata_format";
	final public static String FIELD_METADATA_URL = "metadata_url";
	final public static String FIELD_METADATA_FILE_NAME = "metadata_file_name";

	// field names:
	
	//final public static String FIELD_FILE_ID = "file_id";
	//final public static String FIELD_FILE_URL = "file_url";
    final public static String FIELD_SIZE = "size";
    //#final public static String FIELD_FILE_SIZE = "file_size";
	
	
	// field names: physical data
	final public static String FIELD_PROJECT = "project";
	final public static String FIELD_INSTRUMENT = "instrument";
	final public static String FIELD_VARIABLE = "variable";
	final public static String FIELD_CF_STANDARD_NAME = "cf_standard_name";
	final public static String FIELD_VARIABLE_LONG_NAME = "variable_long_name";
	final public static String FIELD_VARIABLE_UNITS = "variable_units";
	final public static String FIELD_GCMD_TERM = "gcmd_term";
	final public static String FIELD_EXPERIMENT = "experiment";
	final public static String FIELD_EXPERIMENT_FAMILY = "experiment_family";
	
	final public static String FIELD_DATETIME_START = "datetime_start";
	final public static String FIELD_DATETIME_STOP = "datetime_stop";
	
	final public static String FIELD_NORTH = "north_degrees";
	final public static String FIELD_SOUTH = "south_degrees";
	final public static String FIELD_EAST = "east_degrees";
	final public static String FIELD_WEST = "west_degrees";
	final public static String FIELD_HEIGHT_BOTTOM = "height_bottom";
	final public static String FIELD_HEIGHT_TOP = "height_top";
	final public static String FIELD_HEIGHT_UNITS = "height_units";
	
	final public static String FIELD_DATA_FORMAT = "data_format";
	final public static String FIELD_FILE_NAME = "file_name";
			
	/**
	 * Map holding references from record type to Solr core storing those records.
	 */
	final public static Map<String, String> CORES = new HashMap<String,String>();
	
    // required date/time format for Solr documents
    public static String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final DateFormat SOLR_DATE_TIME_FORMATTER = new SimpleDateFormat(SOLR_DATE_FORMAT);

	// static population of Solr cores mapping
	static {
	    
	    CORES.put(QueryParameters.TYPE_DATASET, "datasets");
	    CORES.put(QueryParameters.TYPE_FILE, "files");
	    CORES.put(QueryParameters.TYPE_AGGREGATION, "aggregations");

	    // Solr must ingest dates in UTC=GMT time zone
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        SOLR_DATE_TIME_FORMATTER.setTimeZone(gmt);

	}
	
	/**
	 * Private constructor prevents class instantiation.
	 */
	private SolrXmlPars() {}

}
