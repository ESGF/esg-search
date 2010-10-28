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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import esg.search.query.api.SearchInput;

/**
 * Test class for {@link SolrUrlBuilder}.
 */
public class SolrUrlBuilderTest {
	
	private final static String SOLR_URL = "http://localhost:8983/solr";
	private SolrUrlBuilder solrUrlBuilder;
	
	@Before
	public void setup() throws MalformedURLException {
		solrUrlBuilder = new SolrUrlBuilder(SOLR_URL);
	}
	
	@Test
	public void testBuildUpdateUrl() throws Exception {
		
		// commit=false
		URL url = solrUrlBuilder.buildUpdateUrl(false);
		Assert.assertEquals(SOLR_URL+"/update", url.toString());
		
		// commit=true
		url = solrUrlBuilder.buildUpdateUrl(true);
		Assert.assertEquals(SOLR_URL+"/update?commit=true", url.toString());		
		
	}
	
	@Test
	public void testBuildSelectUrl() throws Exception {
		
		// query default field, match all documents
		final SearchInput input = new SearchInputImpl();
		solrUrlBuilder.setSearchInput(input);
		URL url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=*&start=0&rows=10&distrib=true", url.toString());
		
		// query default field, specify results type as query filter
		input.setType("Dataset");
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=*&fq=type%3A%22Dataset%22&start=0&rows=10&distrib=true", url.toString());
		
		// query default field, use query filter for results type, match text
		input.setText("atmospheric data");
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=atmospheric+data&fq=type%3A%22Dataset%22&start=0&rows=10&distrib=true", url.toString());

		// query default field, use query filter for results type, match text, retrieve all facets
		final List<String> facets = Arrays.asList( new String[]{ "facet1", "facet2" } );
		solrUrlBuilder.setFacets(facets);
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=atmospheric+data&fq=type%3A%22Dataset%22&facet=true&facet.field=facet1&facet.field=facet2&start=0&rows=10&distrib=true", url.toString());
		
		// query default field, use query filter for results type, match text, use facet constraint, retrieve all facets
		input.addConstraint("facet1", "value1");
		url = solrUrlBuilder.buildSelectUrl();
		Assert.assertEquals(SOLR_URL+"/select/?indent=true&q=atmospheric+data&fq=type%3A%22Dataset%22&fq=facet1%3A%22value1%22&facet=true&facet.field=facet1&facet.field=facet2&start=0&rows=10&distrib=true", url.toString());
		
	}

}
