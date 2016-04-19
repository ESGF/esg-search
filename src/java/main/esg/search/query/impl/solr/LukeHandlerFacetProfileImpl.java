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
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.query.api.Facet;
import esg.search.query.api.FacetProfile;
import esg.search.query.api.QueryParameters;
import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;
import esg.security.registry.service.api.RegistryService;

/**
 * Base implementation of {@link FacetProfile} that builds the list of available facets directly from a query to Solr,
 * using the Luke  query handler.
 * Note that at this time the list of available facets is NOT reloaded on demand every time a client requests it,
 * to avoid any performance issues.
 * 
 */
public class LukeHandlerFacetProfileImpl implements FacetProfile, Serializable {
	
	private Map<String, Facet> facets = new LinkedHashMap<String, Facet>();
	
	protected HttpClient httpClient = new HttpClient();

	private final XmlParser xmlParser = new XmlParser(false);
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	private static final long serialVersionUID = 1L;

	private final static String XPATH = "/response/lst[@name='fields']/lst";
	private XPath xPath = null;
	private RegistryService registryService = null;
		
	/**
	 * Constructor that builds the list of facets from a properties file.
	 * @param propertisFile
	 */
	public LukeHandlerFacetProfileImpl(final RegistryService registryService) {
		this.registryService = registryService;
	}
		
	/**
	 * Method that queries Solr for the latest list of facets
	 */
	protected Map<String, Facet> queryFacets() throws Exception {
		
		Map<String, Facet> _facets = new LinkedHashMap<String, Facet>();
		
		LinkedHashSet<String> shards = this.registryService.getShards();
		
		for (String shard : shards) {
		
			final String fullUrl = "http://" + shard + "/datasets/admin/luke/?numTerms=0";
			if (LOG.isInfoEnabled()) LOG.info("Querying all available facets from URL="+fullUrl);
			String response = httpClient.doGet(new URL(fullUrl));
			final Document doc = xmlParser.parseString(response);
			xPath = XPath.newInstance(XPATH);
			for (final Object obj : xPath.selectNodes(doc)) {
				
				String facetKey = ((Element)obj).getAttributeValue("name");
				if (!_facets.containsKey(facetKey)) {  // already counted
					// avoid faceting on fields that have too many values to improve performance
					if (!QueryParameters.NOT_FACETS.contains(facetKey)) {
						_facets.put(facetKey, new FacetImpl(facetKey, facetKey, "")); // facet key = facet name
						 if (LOG.isInfoEnabled()) LOG.info("Using facet:"+facetKey);
					}
				}
				
			}
		}
		
		return _facets;
	}

	/**
	 * Constructor that builds the list of facets from a configuration map composed of (facet key, facet label) pairs.
	 * @param facets
	 */
	public LukeHandlerFacetProfileImpl(final LinkedHashMap<String, String> map) {
		
		for (final String key : map.keySet()) {
			facets.put(key, new FacetImpl(key, map.get(key), ""));
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, Facet> getTopLevelFacets() {
		
		// initialize facets ?
		if (this.facets.isEmpty()) {
			synchronized(this.facets) {
				try {
					Map<String, Facet> newFacets = this.queryFacets();
					if (!newFacets.isEmpty()) {
						this.facets = newFacets;
					}
				} catch(Exception e) {
					LOG.warn(e.getMessage());
				}
			}
		}
		
		return Collections.unmodifiableMap(facets);
	}

}
