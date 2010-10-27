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
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;

public class SearchServiceMain {
	
	private static String[] configLocations = new String[] { "classpath:esg/search/config/search-context.xml" };
	private static final Log LOG = LogFactory.getLog(SearchServiceMain.class);
	
	public static void main(String[] args) throws Exception {
		
		// load instance from Spring configuration
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
		
		final SearchService searchService = (SearchService) context.getBean("searchService");
		
		final FacetProfile facetProfile = (FacetProfile)context.getBean("facetProfile");
				
		
		// test unconstrained facets
		final SearchInput input = new SearchInputImpl();
		
		
		input.setFacets(new ArrayList<String>(facetProfile.getTopLevelFacets().keySet()));
		
		Map<String, Facet> facets = searchService.search(input, true, true).getFacets();
		for (final Facet facet : facets.values()) {
			LOG.info(facet.toString());
		}
		
		
		//added 10-22 - test for geospatial range constraint
		//Note: this assumes CDIAC/AmeriFlux site code=AMF_USARM, Site name=ARM SGP Main, Version=V003 
		//data has been ingested into solr with geospatial data included
		LOG.info("\nQUERY #1A");
		input.setText("USARM");
		SearchOutput output = searchService.search(input, true, true);
		LOG.info(output.toString());

		
		LOG.info("\nQUERY #1B");
		input.addGeospatialRangeConstraint("east_degrees", "[-96 TO *]");
		output = searchService.search(input, true, true);
		LOG.info(output.toString());

		
		/*
		// text query
		LOG.info("\nQUERY #1");
		input.setText("boreas");
		SearchOutput output = searchService.search(input, true, true);
		LOG.info(output.toString());

		
		
		// text + 1 facet query
		LOG.info("\nQUERY #2");
		input.addConstraint("project", "EOSDIS");
		output = searchService.search(input, true, true);
		LOG.info(output.toString());
		
		// text + 2 facets query
		LOG.info("\nQUERY #3");
		input.addConstraint("gcmd_variable", "LAND SURFACE > SOILS > SOIL TEMPERATURE");
		output = searchService.search(input, true, true);
		LOG.info(output.toString());
		
		// constrained facets
		LOG.info("\nQUERY #4");
		facets = searchService.search(input, true, true).getFacets();
		for (final Facet facet : facets.values()) {
			LOG.info(facet.toString());
		}
		
		// text + 3 facets query
		LOG.info("\nQUERY #5");
		input.addConstraint("instrument", "TEMPERATURE SENSOR");
		output = searchService.search(input, true, true);
		LOG.info(output.toString());
		
		// empty query for results
		LOG.info("\nQUERY #6");
		input.addConstraint("frequency", "Daily");
		output = searchService.search(input, true, true);
		LOG.info(output.toString());
		
		// empty query for facets
		LOG.info("\nQUERY #7");
		facets = searchService.search(input, true, true).getFacets();
		for (final Facet facet : facets.values()) {
			LOG.info(facet.toString());
		}
		*/
		
	}

	

}
