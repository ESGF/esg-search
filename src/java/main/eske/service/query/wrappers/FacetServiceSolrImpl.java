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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;
import eske.model.query.Facet;
import eske.model.query.FacetUtils;
import eske.service.query.api.FacetService;
import eske.service.query.api.FacetServiceListener;
import eske.service.query.api.QueryException;

/**
 * Implementation of ESKE {@link FacetService} based on Apache-Solr back-end.
 * The only time when this class executes an actual query is in response to the init() method invocation,
 * otherwise it simply translates the stored facet objects into the requested format.
 */
public class FacetServiceSolrImpl implements FacetService {
	
	/**
	 * The underlying {@link SearchService} to which all functionality is delegated.
	 */
	private final SearchService searchService;
	
	/**
	 * Utility class to translate facet and product keys.
	 */
	private final MappingUtil mappingUtil;
	
	/**
	 * Factory class to translate facet objects.
	 */
	private final FacetFactory facetFactory;
	
	/**
	 * Set of clients that are notified of changes in the content of the facets.
	 */
	private Set<FacetServiceListener> listeners = new HashSet<FacetServiceListener>();
	
	/**
	 * Ordered map of search facets, indexed by facet key. 
	 * The unconstrained search facets are stored by the service to boost performance, 
	 * and can recomputed by invoking the init method.
	 */
	private Map<String, esg.search.query.api.Facet> facets = new LinkedHashMap<String, esg.search.query.api.Facet>();

	private static final Log LOG = LogFactory.getLog(FacetServiceSolrImpl.class);
	
	/**
	 * Constructor must be supplied with a {@link SearchService} and a map to translate facet keys.
	 * @param searchService
	 */
	public FacetServiceSolrImpl(final SearchService searchService, final MappingUtil mappingUtil) {
		this.searchService = searchService;
		this.mappingUtil = mappingUtil;
		facetFactory = new FacetFactory(mappingUtil);
	}
	
	/**
	 * Initialization method recomputes the search facets by querying the Solr server.
	 */
	public void init() throws QueryException {

		try {
			
			// execute unconstrained search for all facets specified in map values
			final SearchInput input = new SearchInputImpl();
			input.setFacets(new ArrayList<String>( mappingUtil.getInverseFacetKeys()) );
			final Map<String, esg.search.query.api.Facet> newFacets = this.searchService.getFacets(input);
	
			// swap the maps
			synchronized (facets) {
				facets = newFacets;
			}
			
			// notify registered listeners
			for (final FacetServiceListener listener : listeners) {
				if (LOG.isDebugEnabled()) LOG.debug("Reloading FacetServiceListener:"+listener.getClass().getName());
				listener.reload();
			}
			
		} catch(Exception e) {
			throw new QueryException(e);
		}

	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This implementation returns the keys as defined in the constructor map.
	 */
	public Set<String> getFacetNames() throws QueryException {
		return Collections.unmodifiableSet( mappingUtil.getFacetKeys() );
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This implementation delegates the work of building an ESKE facet to the {@link FacetFactory} class.
	 */
	public eske.model.query.Facet getFacet(final String facetKey) throws QueryException {
		
		final String _facetKey = mappingUtil.getFacetMapping(facetKey);
		return facetFactory.getFacetInstance(facets.get( _facetKey ));
			
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Map<eske.model.query.Facet, String>> getFacetsHierarchyMap() throws QueryException {
		
		// loop over facets supported by the FacetService, store hierarchies for UI
		final Map<String, Map<Facet,String>> _facetsHierarchyMap = new HashMap<String, Map<Facet,String>>();
		
		for (String fname : this.getFacetNames()) {
			if (LOG.isDebugEnabled()) LOG.debug("Retrieving facet:"+fname);
			
			final eske.model.query.Facet facet = this.getFacet(fname);  // retrieve facet hierarchy from repository
			final Map<eske.model.query.Facet,String> fhierarchy = FacetUtils.toHierarchyListedMap(facet, true); // skip root facet
			if (LOG.isDebugEnabled()) {
				for (eske.model.query.Facet f : fhierarchy.keySet()) {
					 LOG.debug("\tFacet hierarchy:"+fhierarchy.get(f) );
				}
			}
			_facetsHierarchyMap.put(fname, fhierarchy);
			
		} // loop over facets

		return _facetsHierarchyMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public void subscribe(final FacetServiceListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unsubscribe(final FacetServiceListener listener) {
		this.listeners.remove(listener);
	}

}
