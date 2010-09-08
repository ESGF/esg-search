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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Factory class to convert a {@link esg.search.query.Facet} instance into a {@link eske.model.query.Facet} instance. 
 */
public class FacetFactory {
	
	/**
	 * Utility class to translate facet and product keys.
	 */
	private final MappingUtil mappingUtil;
	
	public FacetFactory(final MappingUtil mappingUtil) {
		
		this.mappingUtil = mappingUtil;	

	}
	
	/**
	 * Factory method that transforms one hierarchy of facet objects into another.
	 * @param facetKey : the key to assign to the newly created eske.model.query.Facet
	 * @param facet : the input esg.search.api.Facet
	 * @return : the output eske.model.query.Facet
	 */
	public eske.model.query.Facet getFacetInstance(esg.search.query.api.Facet facet) {
		
		final String _facetKey = mappingUtil.getInverseFacetMapping(facet.getKey());
		final eske.model.query.Facet _facet = new eske.model.query.Facet(_facetKey, facet.getLabel(), facet.getDescription());
		for (final esg.search.query.api.Facet subFacet : facet.getSubFacets()) {
			final eske.model.query.Facet _subFacet = new eske.model.query.Facet(subFacet.getKey(), subFacet.getLabel(), subFacet.getDescription());
			_facet.addSubFacet(_subFacet);
		}
		return _facet;

	}
	
	/**
	 * Utility method that translates a map of {@link esg.search.query.Facet} objects into a map of string options.
	 * @param facets
	 * @return
	 */
	public Map<String, Set<String>> getFacetMap(final Map<String, esg.search.query.api.Facet> facets) {
		
		final Map<String, Set<String>> _facets = new HashMap<String, Set<String>>();
		
		for (final String facetKey : facets.keySet()) {

			final String _facetKey = mappingUtil.getInverseFacetMapping(facetKey);
			final Set<String> options = new LinkedHashSet<String>();
			for (final esg.search.query.api.Facet subFacet : facets.get(facetKey).getSubFacets()) {
				options.add(subFacet.getKey());
			}
			_facets.put(_facetKey, options);
			
		}
		
		return _facets;
	}

}
