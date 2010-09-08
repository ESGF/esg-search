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
import java.util.List;

import esg.search.query.api.Facet;

public class FacetImpl implements Facet {
	
	/**
	 * The facet key used for programmatic access (immutable).
	 */
	private final String key;
	
	/**
	 * The facet label displayed to human users (immutable).
	 */
	private final String label;
	
	/**
	 * Optional facet description (immutable).
	 */
	private final String description;
	
	/**
	 * The current facet count.
	 */
	private int counts;
	
	/**
	 * The current list of sub-facets.
	 */
	private List<Facet> subFacets = new ArrayList<Facet>();
	
	/**
	 * Constructor sets the immutable properties of the facet.
	 * @param key
	 * @param label
	 * @param description
	 */
	public FacetImpl(final String key, final String label, final String description) {
		this.key = key;
		this.label = label;
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSubFacet(Facet subFacet) {
		this.subFacets.add(subFacet);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCounts() {
		return this.counts;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel() {
		return label;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Facet> getSubFacets() {
		return Collections.unmodifiableList(this.subFacets);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("FacetImpl [key="+key+" label="+label+" description="+description+" counts=" + counts + "]\n");
		for (final Facet sf : this.getSubFacets()) {
			sb.append(sf.toString());
		}
		return sb.toString();
	}

}
