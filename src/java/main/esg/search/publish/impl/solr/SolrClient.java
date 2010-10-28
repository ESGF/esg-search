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
package esg.search.publish.impl.solr;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.publish.api.RecordConsumer;
import esg.search.query.impl.solr.SolrUrlBuilder;
import esg.search.utils.HttpClient;

/**
 * Abstract implementation of {@link RecordConsumer} that sends records to a remote Solr server.
 * Specific sub-classes define how the records are consumed (inserted, removed, etc.).
 */
public abstract class SolrClient implements RecordConsumer {
					
	protected final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Instance attribute shared among all HTTP request,
	 * since the generation of the POST URL does not depend on the instance's state.
	 */
	protected SolrUrlBuilder solrUrlBuilder;
	
	/**
	 * Client used to execute HTTP/POST requests.
	 */
	protected HttpClient httpClient = new HttpClient();
	
	/**
	 * Constructor initializes the URL builder.
	 * @param url
	 */
	public SolrClient(final String url) {
		
		solrUrlBuilder = new SolrUrlBuilder(url);
		
	}


}
