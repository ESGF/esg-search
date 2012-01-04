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

import org.apache.bcel.generic.GETSTATIC;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;
import esg.search.utils.HttpClient;
import esg.security.registry.service.api.RegistryService;

/**
 * Implementation of {@link SearchService} based on an Apache-Solr back-end.
 * This service is configured to send and receive XML messages to a fixed Solr server specified by the constructor URL argument. 
 * 
 * The URL for the HTTP/GET request is built by the collaborator bean {@link SolrUrlBuilder} based on the content
 * of the {@link SearchInput} instance, while the content of the HTTP response is parsed by the collaborator bean
 * {@link SolrXmlPars}. 
 * 
 * An optional {@link RegistryService} can be used to provide a list of query endpoints for distributed searches.
 * 
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
	
	/**
	 * Optional registry service providing list of query endpoints for distributed search.
	 */
	private RegistryService registryService = null;

	private static final Log LOG = LogFactory.getLog(SearchServiceImpl.class);

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
	public SearchOutput search(final SearchInput input) throws Exception {
		
		// execute HTTP request, return XML
		final String response = this.query(input, SearchReturnType.SOLR_XML);		
		
		// parse HTTP XML response into Java object
		final SearchOutput output = xmlParser.parse(response, input);
		
		return output;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String query(final SearchInput input, final SearchReturnType returnType) throws Exception {
		
	    if (LOG.isInfoEnabled()) LOG.info("Query Input:\n"+input.toString());
	    
		// formulate HTTP request
		final SolrUrlBuilder builder = new SolrUrlBuilder(url);
		builder.setSearchInput(input);
		builder.setFacets(input.getFacets());
		if (registryService!=null) builder.setShards( registryService.getShards() );
		final URL request = builder.buildSelectUrl();		
		
		// execute HTTP request, return response as Solr/XML or Solr/JSON
		String output = httpClient.doGet(request);
		
		// transform to requested format
		final String response = this.transform(output, returnType);
		
		return response;
		
	}
	
	private String transform(final String output, final SearchReturnType returnType) throws Exception {
	    
	    if (returnType==SearchReturnType.SOLR_XML || returnType==SearchReturnType.SOLR_JSON) {
	        return output;
	    } else {
	        throw new Exception("Unsupported output format: "+returnType.getMimeType());
	    }
	    
	}

	@Autowired
    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }
	

}
