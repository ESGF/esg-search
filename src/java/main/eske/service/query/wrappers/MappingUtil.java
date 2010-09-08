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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.util.Assert;

/**
 * Utility class that translates facet and product keys from the ESKE model to the ESG model.
 * Reverse mappings are also made available. 
 * To simplify code, runtime exceptions are thrown when the requested mappings are not found.
 */
public class MappingUtil {
	
	/**
	 * Map that translates {@link eske.model.query.Facet} keys into {@link esg.search.query.Facet} keys.
	 */
	private final Map<String,String> facetKeyMap;
	
	/**
	 * Map that translates product values from ESKE to ESG model.
	 */
	private final Map<String,String> productValueMap;
	
	/**
	 * Reversed map that translates {@link esg.search.query.Facet} keys into {@link eske.model.query.Facet} keys.
	 */
	private final Map<String,String> reverseFacetKeyMap = new LinkedHashMap<String,String>();
	
	/**
	 * Map that translates product values from ESG to ESKE model.
	 */
	private final Map<String,String> reverseProductValueMap = new LinkedHashMap<String, String>();

	
	public MappingUtil(final Map<String,String> facetKeyMap, final  Map<String,String> productValueMap) {
		
		this.facetKeyMap = facetKeyMap;
		
		// reverse the facet key map
		for (final Entry<String,String> entry : facetKeyMap.entrySet()) {
			reverseFacetKeyMap.put(entry.getValue(), entry.getKey());
		}
		
		this.productValueMap = productValueMap;
		
		// reverse the product value map
		for (final Entry<String,String> entry : productValueMap.entrySet()) {
			reverseProductValueMap.put(entry.getValue(), entry.getKey());
		}
		
	}
	
	/**
	 * Translates a {@link eske.model.query.Facet} key into a {@link esg.search.query.Facet} key.
	 * @param facetKey
	 * @return
	 */
	public String getFacetMapping(final String facetKey) {
		Assert.isTrue( facetKeyMap.containsKey(facetKey) );
		return facetKeyMap.get(facetKey);
	}
	
	/**
	 * Translates a {@link esg.search.query.Facet} key into a {@link eske.model.query.Facet} key.
	 * @param facetKey
	 * @return
	 */
	public String getInverseFacetMapping(final String facetKey) {
		Assert.isTrue( reverseFacetKeyMap.containsKey(facetKey) );
		return reverseFacetKeyMap.get(facetKey);
	}
	
	/**
	 * Translates a {@link eske.model.query.QueryInput} product value into a {@link esg.search.query.SearchInput} product value.
	 * @param facetKey
	 * @return
	 */
	public String getProductMapping(final String productValue) {
		Assert.isTrue( productValueMap.containsKey(productValue) );
		return productValueMap.get(productValue);
	}
	
	/**
	 * Translates a {@link esg.search.query.SearchInput} product value into a {@link eske.model.query.QueryInput} product value.
	 * @param facetKey
	 * @return
	 */
	public String getInverseProductMapping(final String productValue) {
		Assert.isTrue( reverseProductValueMap.containsKey(productValue) );
		return reverseProductValueMap.get(productValue);
	}
	
	/**
	 * Method to return an (ordered) set of the supported ESKE facet keys.
	 * @return
	 */
	public Set<String> getFacetKeys() {
		return Collections.unmodifiableSet( this.facetKeyMap.keySet() );
	}
	
	/**
	 * Method to return an (ordered) set of the supported ESG facet keys.
	 * @return
	 */
	public Set<String> getInverseFacetKeys() {
		return Collections.unmodifiableSet( this.reverseFacetKeyMap.keySet() );
	}

}
