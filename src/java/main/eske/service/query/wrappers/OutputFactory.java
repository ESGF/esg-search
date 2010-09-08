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
package eske.service.query.wrappers;

import java.util.List;

import esg.search.core.Record;
import esg.search.query.api.SearchOutput;
import esg.search.query.impl.solr.SolrXmlPars;
import eske.model.query.QueryOutput;
import eske.model.query.QueryResult;
import eske.service.query.api.QueryException;

/**
 * Factory to translate the output of a search operation.
 */
public class OutputFactory {
	
	/**
	 * Utility class to translate facet and product keys.
	 */
	private final MappingUtil mappingUtil;
	
	/**
	 * Constructor must be supplied with maps to translate facet and product keys.
	 * @param facetKeyMap
	 */
	public OutputFactory(final MappingUtil mappingUtil) {
		this.mappingUtil = mappingUtil;
	}

	/**
	 * Factory method to translate the full search output.
	 * @param output
	 * @return
	 */
	public QueryOutput getInstance(final SearchOutput output) throws QueryException {
		
		final QueryOutput _output = new QueryOutput();
		
		// number of results
		_output.setCounts(output.getCounts());
		
		// loop over results
		for (final Record record : output.getResults()) {
			_output.addResult( getInstance(record), true); // first=true
		}
		
		return _output;
		
	}
	
	/**
	 * Factory method to translate a single search result.
	 * @param record
	 * @return
	 */
	public QueryResult getInstance(final Record record) throws QueryException {
		
		final QueryResult result = new QueryResult(record.getId());
		for (final String name : record.getFields().keySet()) {
			final List<String> values = record.getFields().get(name);
			if (name.equals(SolrXmlPars.FIELD_TITLE) || name.equals(SolrXmlPars.FIELD_NAME)) {
				result.setName( values.get(0) );
			} else if (name.equals(SolrXmlPars.FIELD_DESCRIPTION)) {
				result.setDescription( values.get(0) );
			} else if (name.equals(SolrXmlPars.FIELD_URL)) {
				result.setUrl( values.get(0) );
			} else if (name.equals(SolrXmlPars.FIELD_TYPE)) {
				final String value = values.get(0);
				result.setType( mappingUtil.getInverseProductMapping(value) );
			} else {
				for (final String value : values) {
					result.getFacets().addValue(name, value, value);
				}
			}
		}
		
		return result;
	}
		
}
