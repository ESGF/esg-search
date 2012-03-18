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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchReturnType;

/**
 * Standard bean implementation of {@link SearchInput}.
 */
public class SearchInputImpl implements SearchInput, Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * The free query used in the query.
	 */
	private String query;
	
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
	 * Set of optional and standard fields to be returned for each result.
	 */
	private Set<String> fields = new HashSet<String>();
	
	/**
	 * The offset into the number of returned results.
	 */
	private int offset = 0;
	
	/**
	 * The maximum number of results to be returned.
	 */
	private int limit = 10;
	
	/**
	 * Flag to execute a distributed query - true by default.
	 */
	private boolean distrib = true;
	
	/**
	 * Requested format for query results, defaults to Solr/XML.
	 */
	private String format = SearchReturnType.SOLR_XML.getMimeType();
	
	/**
	 * Set of shards to query, if specified.
	 */
	private LinkedHashSet<String> shards = new LinkedHashSet<String>();
	
	/**
	 * Lower bound on last update, if specified.
	 */
	private String from = "";
	
	/**
     * Upper bound on last update, if specified.
     */
	private String to = "";
	
	/**
	 * Don't sort records by default
	 */
	private boolean sort = false;
	
	private final static String NEWLINE = System.getProperty("line.separator");
	
	/**
	 * Constructor sets the results type.
	 * @param type
	 */
	public SearchInputImpl(String type) {
	    this.setConstraint(QueryParameters.FIELD_TYPE, type);
	}

	
	@Override
    public void setConstraint(String name, String value) {
        if (StringUtils.hasText(name) && StringUtils.hasText(value)) {
            constraints.put(name, new ArrayList<String>());
            this.constraints.get(name).add(value);
        }
    }

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
	public String getConstraint(final String name) {
	    if (constraints.containsKey(name)) return constraints.get(name).get(0);
	    else return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setQuery(final String query) {
		this.query = query;
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
	public boolean isSort() {
        return sort;
    }

    /**
     * {@inheritDoc}
     */
    public void setSort(boolean sort) {
        this.sort = sort;
    }

    /**
	 * Overridden method to print the instance content.
	 */
	@Override
	public String toString() {
		
		final StringBuilder s = new StringBuilder();
		// distributed search
		s.append("Distributed Search:"+this.isDistrib()).append(NEWLINE);
		// query
	    s.append("Search Text:"+this.getQuery()).append(NEWLINE);
	    // offset, limit
        s.append("Search offset: "+offset+" ").append(" limit: ").append(limit).append(NEWLINE);
        // format
        s.append("Output Format: ").append(this.format).append(NEWLINE);
		// geospatialRangeconstraints
		if (StringUtils.hasText(this.geospatialRangeConstraint)) s.append("Geo-spatial Constraint: " + this.geospatialRangeConstraint).append(NEWLINE);
		// temporal constraint
		if (StringUtils.hasText(this.temporalRangeConstraint)) s.append("Temporal Constraint: " + this.temporalRangeConstraint).append(NEWLINE);
	    // last update range
        if (StringUtils.hasText(this.from)) s.append("From Last Update: " + this.from).append(NEWLINE);
        if (StringUtils.hasText(this.to)) s.append("To Last Update: " + this.to).append(NEWLINE);
		// facets
		for (final String facet : facets) {
			s.append("Search Facet: ").append(facet).append(NEWLINE);
		}
	    // fields
        for (final String field : fields) {
            s.append("Returned Field: ").append(field).append(NEWLINE);
        }
        // shards
        for (final String shard : shards) {
            s.append("Queried shard: ").append(shard).append(NEWLINE);
        }
        // facet constraints
        for (final String name : this.constraints.keySet()) {
            s.append("Search Constraint: ").append(name).append("=");
            for (final String value : this.constraints.get(name)) {
                s.append(value).append(" ");
            }
            s.append(NEWLINE);
        }

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


    @Override
    public boolean isDistrib() {
        return distrib;
    }


    @Override
    public void setDistrib(boolean distrib) {
        this.distrib = distrib;
    }

    @Override
    public Set<String> getFields() {
        return fields;
    }

    @Override
    public void setFields(final Set<String> fields) {
        this.fields = fields;
    }


    @Override
    public void setFormat(final String format) {
       this.format = format;
    }


    @Override
    public String getFormat() {
        return format;
    }


    @Override
    public void setShards(final Set<String> shards) {
        this.shards.clear();
        this.shards.addAll(shards);
    }

    @Override
    public Set<String> getShards() {
        return shards;
    }


    @Override
    public String getFrom() {
        return from;
    }


    @Override
    public void setFrom(final String from) {
        this.from = from;
    }


    @Override
    public String getTo() {
        return to;
    }


    @Override
    public void setTo(final String to) {
        this.to = to;
    }
    
    
	
}
