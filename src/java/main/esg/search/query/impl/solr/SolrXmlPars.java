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

import java.util.HashMap;
import java.util.Map;

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
	
	final public static String  ELEMENT_FACET_COUNTS = "facet_counts";
	final public static String  ELEMENT_FACET_FIELDS = "facet_fields";
	
	final public static String ATTRIBUTE_NAME = "name";
	final public static String ATTRIBUTE_NUM_FOUND = "numFound";
	final public static String ATTRIBUTE_START = "start";
	
	final public static String ATTRIBUTE_VALUE_RESPONSE = "response";
		
	/**
	 * Map holding references from record type to Solr core storing those records.
	 */
	final public static Map<String, String> CORES = new HashMap<String,String>();

	// static population of Solr cores mapping
	static {
	    CORES.put(QueryParameters.TYPE_DATASET, "datasets");
	    CORES.put(QueryParameters.TYPE_FILE, "files");
	}
	
	/**
	 * Private constructor prevents class instantiation.
	 */
	private SolrXmlPars() {}

}
