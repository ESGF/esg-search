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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;
import esg.search.utils.HttpClient;

/**
 * Implementation of {@link SearchService} based on an Apache-Solr back-end.
 * This service is configured to send and receive XML messages to a fixed Solr server specified by the constructor URL argument. 
 * 
 * The URL for the HTTP/GET request is built by the collaborator bean {@link SolrUrlBuilder} based on the content
 * of the {@link SearchInput} instance, while the content of the HTTP response is parsed by the collaborator bean
 * {@link SolrXmlPars}. 
 */
@Service("searchService")
public class SearchServiceImpl implements SearchService {
	
	/**
	 * The base URL of the Apache-Solr server.
	 */
	private final URL url;

	/**
	 * The client used to communicate with the Solr server via its REST API.
	 */
	private final HttpClient httpClient = new HttpClient();

	/**
	 * The parser used to parse the XML output from the server.
	 */
	final SolrXmlParser xmlParser = new SolrXmlParser();

	//private static final Log LOG = LogFactory.getLog(SearchServiceImpl.class);

	/**
	 * Constructor with mandatory arguments.
	 * 
	 * @param url
	 * @throws MalformedURLException
	 */
	@Autowired
	public SearchServiceImpl(final @Value("${esg.search.solr.query.url}") URL url) throws MalformedURLException {
		this.url = url;
	}

	/**
	 * {@inheritDoc}
	 */
	public SearchOutput search(final SearchInput input, final boolean getResults, final boolean getFacets) throws Exception {
		
		// execute HTTP request, return XML
		final String response = this.query(input, getResults, getFacets, SearchReturnType.XML);		
		
		// parse HTTP XML response into Java object
		final SearchOutput output = xmlParser.parse(response, input, getResults, getFacets);
		
		return output;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String query(final SearchInput input, final boolean getResults, final boolean getFacets, final SearchReturnType returnType) throws Exception {
		
		// formulate HTTP request
		final SolrUrlBuilder builder = new SolrUrlBuilder(url);
		builder.setSearchInput(input);
		if (getFacets) builder.setFacets(input.getFacets());
		final URL request = builder.buildSelectUrl();		
		
		// execute HTTP request, return response
		return httpClient.doGet(request);
		
		
	}
	

}
