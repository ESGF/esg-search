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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.query.api.Facet;
import esg.search.query.api.FacetProfile;
import esg.search.utils.PropertiesUtils;

/**
 * Base implementation of {@link FacetProfile} initialized from a map of (facet key, facet label) pairs.
 */
public class FacetProfileImpl implements FacetProfile, Serializable {
	
	private Map<String, Facet> facets = new LinkedHashMap<String, Facet>();
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor that builds the list of facets from a properties file.
	 * @param propertisFile
	 */
	public FacetProfileImpl(final String propertiesFilePath) {
	    final Properties properties = PropertiesUtils.load(propertiesFilePath);	    
	    for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
	        String key = (String) iter.next();
	        String value = (String) properties.get(key);
	        facets.put(key, new FacetImpl(key, value, ""));
	        if (LOG.isInfoEnabled()) LOG.info("Using facet:"+key+" label="+value);
	      }
	}

	/**
	 * Constructor that builds the list of facets from a configuration map composed of (facet key, facet label) pairs.
	 * @param facets
	 */
	public FacetProfileImpl(final LinkedHashMap<String, String> map) {
		
		for (final String key : map.keySet()) {
			facets.put(key, new FacetImpl(key, map.get(key), ""));
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, Facet> getTopLevelFacets() {
		return Collections.unmodifiableMap(facets);
	}

}
