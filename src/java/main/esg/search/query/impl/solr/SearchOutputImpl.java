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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import esg.search.core.Record;
import esg.search.query.api.Facet;
import esg.search.query.api.SearchOutput;

/**
 * Standard bean implementation of {@link SearchOutput}.
 */
public class SearchOutputImpl implements SearchOutput {
	
	/**
	 * The list of records returned by the search.
	 */
	private List<Record> results = new ArrayList<Record>();
	
	/**
	 * The map of facets returned by the search.
	 */
	private Map<String, Facet> facets = new LinkedHashMap<String, Facet>();
	
	/**
	 * The total number of records matching the search input criteria.
	 */
	private int counts = 0;

	/**
	 * The paging offset into the returned results.
	 */
	private int offset = 0;
	
	private final static String NEWLINE = System.getProperty("line.separator");
	
	/**
	 * {@inheritDoc}
	 */
	public void addResult(final Record record) {
		this.results.add(record);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Record> getResults() {
		return Collections.unmodifiableList(results);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, Facet> getFacets() {
		return Collections.unmodifiableMap(facets);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addFacet(final String key, final Facet facet) {
		facets.put(key, facet);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getCounts() {
		return counts;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCounts(int counts) {
		this.counts = counts;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * Overridden method to print out the {@link Record} content.
	 */
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		s.append("Total number of results=").append(this.counts).append(NEWLINE);
		int i=0;
		for (final Record r : this.getResults()) {
			s.append("Result #").append(i++).append(": ")
			 .append(r.toString()).append(NEWLINE);
		}
		s.append("Facets:").append(this.getFacets());
		return s.toString();
	}

}
