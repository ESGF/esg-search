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

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eske.model.query.FacetUtils;
import eske.model.query.QueryInput;
import eske.model.query.QueryOutput;
import eske.model.query.QueryResult;
import eske.service.query.api.FacetService;
import eske.service.query.api.QueryService;

public class QueryWrappersMain {
	
	private static String[] configLocations = new String[] { 
		"classpath:esg/search/config/search-context.xml",
		"classpath:eske/service/query/wrappers/query-wrappers.xml"
	};
	private static final Log LOG = LogFactory.getLog(QueryWrappersMain.class);
	
	private final static String NEWLINE = System.getProperty("line.separator");
	
	public static void main(String[] args) throws Exception {
		
		// load instance from Spring configuration
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
		final FacetService facetService = (FacetService) context.getBean("facetService");
		final QueryService queryService = (QueryService) context.getBean("queryService");

		final Map<String, String> facetKeyMap = (Map<String,String>) context.getBean("facetKeyMap");
		
		// test facet wrapper
		
		final Set<String> facetNames = facetService.getFacetNames();
		for (final String fname : facetNames) {
			LOG.info("Facet name="+fname);
			eske.model.query.Facet facet = facetService.getFacet(fname);
			FacetUtils.print(facet);
		}
		
		final Map<String, Map<eske.model.query.Facet,String>> fhierarchy = facetService.getFacetsHierarchyMap();
		FacetUtils.printAll(fhierarchy);
		
		// test query wrapper
		
		LOG.info(NEWLINE);
		final QueryInput queryInput = new QueryInput();
		queryInput.addConstraint("hasText", "commit");
		QueryOutput queryOutput = queryService.queryResults(queryInput);
		
		LOG.info(NEWLINE);
		final String id = "2";
		final QueryResult result = queryService.queryResult(id);
		
		LOG.info(NEWLINE);
		Map<String, Set<String>> facets = queryService.queryFacets(facetKeyMap.keySet(), queryInput);
		printMap(facets);
		
		LOG.info(NEWLINE);
		Set<String> options = queryService.queryFacet("hasRealm", queryInput);
		for (final String option : options) {
			LOG.info("facet value="+option);
		}
		
		LOG.info(NEWLINE);
		queryInput.addConstraint("hasRealm", "Land");
		facets = queryService.queryFacets(facetKeyMap.keySet(), queryInput);
		printMap(facets);
		
		LOG.info(NEWLINE);
		queryInput.addConstraint("hasRealm", "Land");
		facets = queryService.queryFacets(queryInput);
		printMap(facets);
		
	}
	
	private static void printMap(final Map<String, Set<String>> facets) {
		for (final String key : facets.keySet()) {
			LOG.info("Facet key="+key);
			for (final String value : facets.get(key)) {
				LOG.info("\tvalue="+value);
			}
		}
	}

}
