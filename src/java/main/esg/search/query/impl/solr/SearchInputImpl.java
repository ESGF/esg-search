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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import esg.search.query.api.SearchInput;

/**
 * Standard bean implementation of {@link SearchInput}.
 */
public class SearchInputImpl implements SearchInput, Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * The free text used in the query.
	 */
	private String text;
	
	/**
	 * The map of constraints to be used in the query, composed of (name, values) pairs.
	 */
	private Map<String, List<String>> constraints = new LinkedHashMap<String, List<String>>();
	
	/**
	  * The string of geospatial range constraints to be used in the query
	  */
	private String geospatialRangeConstraint;
	
	/**
	  * The string of temporal range constraints to be used in the query
	  */
	private String temporalRangeConstraint;
	 
	
	/**
	 * The ordered list of facets to be returned in the search output.
	 */
	private List<String> facets = new ArrayList<String>();
	
	/**
	 * The results type,if not null it must match a specific filter query handler.
	 */
	private String type = null;

	/**
	 * The offset into the number of returned results.
	 */
	private int offset = 0;
	
	/**
	 * The maximum number of results to be returned.
	 */
	private int limit = 10;
	
	private final static String NEWLINE = System.getProperty("line.separator");

	/**
	 * {@inheritDoc}
	 */
	public void addConstraint(final String name, final String value) {
		if (StringUtils.hasText(name) && StringUtils.hasText(value)) {
			if (!constraints.containsKey(name)) {
				constraints.put(name, new ArrayList<String>());
			}
			this.constraints.get(name).add(value);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void removeConstraint(String name) {
		constraints.remove(name);
	}



	/**
	 * {@inheritDoc}
	 */
	public void addConstraints(final String name, final List<String> values) {
		if (StringUtils.hasText(name) && !values.isEmpty()) {
				this.constraints.put(name, values);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<String>> getConstraints() {
		return Collections.unmodifiableMap(constraints);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText() {
		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setText(final String text) {
		this.text = text;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLimit() {
		return limit;
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
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getFacets() {
		return Collections.unmodifiableList(facets);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addFacet(final String facet) {
		this.facets.add(facet);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFacets(final List<String> facets) {
		this.facets = facets;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(final String type) {
		this.type = type;
	}
	
	/**
	 * Overridden method to print the instance content.
	 */
	@Override
	public String toString() {
		
		// text
		final StringBuilder s = new StringBuilder();
		s.append("Search Text:"+this.getText()).append(NEWLINE);
		// constraints
		for (final String name : this.constraints.keySet()) {
			s.append("Search Constraint: ").append(name).append("=");
			for (final String value : this.constraints.get(name)) {
				s.append(value).append(" ");
			}
			s.append(NEWLINE);
		}
		// geospatialRangeconstraints
		s.append("Search Constraint: " + this.geospatialRangeConstraint);
		//end add
		// facets
		for (final String facet : facets) {
			s.append("Search Facet: ").append(facet).append("=").append(facet).append(NEWLINE);
		}
		// offset, limit
		s.append("Search offset: "+offset+" ").append(" limit: ").append(limit);
		return s.toString();
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void addGeospatialRangeConstraint(String geospatialRangConstraint) {
		this.geospatialRangeConstraint = geospatialRangConstraint;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public String getGeospatialRangeConstraint() {
		return this.geospatialRangeConstraint;
	}


	/**
	  * {@inheritDoc}
	  */
    public void addTemporalRangeConstraint(String temporalRangeConstraint) {
        this.temporalRangeConstraint = temporalRangeConstraint;
    }


    /**
     * {@inheritDoc}
     */
    public String getTemporalRangeConstraint() {
        return this.temporalRangeConstraint;
    }
	
}
