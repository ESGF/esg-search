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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.query.api.Facet;
import esg.search.query.api.FacetProfile;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchService;

/**
 * Test program to query the ESGF Search Services.
 * To run locally:
 * cd build
 * java -Djava.ext.dirs=../lib/fetched esg.search.query.impl.solr.SearchServiceMain
 * 
 * @author Luca Cinquini
 *
 */
public class SearchServiceMain {
	
	private static String[] configLocations = new String[] { "classpath:esg/search/config/web-application-context.xml" };
	private static final Log LOG = LogFactory.getLog(SearchServiceMain.class);
	
	public static void main(String[] args) throws Exception {
		
		// load instance from Spring configuration
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
		
		final SearchService searchService = (SearchService) context.getBean("searchService");
		
		final FacetProfile facetProfile = (FacetProfile)context.getBean("wsFacetProfile");
				
		
		// test unconstrained facets
		final SearchInput input = new SearchInputImpl(QueryParameters.DEFAULT_TYPE);
		
		
		input.setFacets(new ArrayList<String>(facetProfile.getTopLevelFacets().keySet()));
		
		Map<String, Facet> facets = searchService.search(input).getFacets();
		for (final Facet facet : facets.values()) {
			LOG.info(facet.toString());
		}
		
	}

	

}
