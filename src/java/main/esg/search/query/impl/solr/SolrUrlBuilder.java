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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import esg.search.query.api.SearchInput;

/**
 * Utility class to generate URL according to the Solr REST API.
 */
public class SolrUrlBuilder {
	
	/**
	 * The base URL of the Solr server.
	 */
	private final URL url;
	
	/**
	 * The search input constraints.
	 */
	private SearchInput input;
	
	/**
	 * The facets to be retrieved as part of the search.
	 */
	private List<String> facets;
	
	/**
	 * Flag for pretty-formatting of output.
	 */
	//private boolean indent = false;
	
	private final static String UTF8 = "UTF-8";
	
	private static final Log LOG = LogFactory.getLog(SolrUrlBuilder.class);
	
	/**
	 * Constructor is initialized with the base URL of the Apache-Solr server.
	 * @param url
	 * @throws MalformedURLException
	 */
	public SolrUrlBuilder(final URL url) {
		this.url = url;
	}
	
	/**
	 * Method to set the search constraints to be included in the query part of URL.
	 * @param input
	 */
	public void setSearchInput(final SearchInput input) {
		this.input = input;
	}
	
	/**
	 * Method to set the search facet keys to be retrieved as part of the search.
	 * @param facets
	 */
	public void setFacets(final List<String> facets) {
		this.facets = facets;
	}
	
	/**
	 * Method to set the flag for output indentation.
	 * @param indent
	 */
	//public void setIndent(final boolean indent) {
	//	this.indent = indent;
	//}
	
	/**
	 * Method to generate the "update" URL.
	 * This method is independent of the specific state of the object.
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public URL buildUpdateUrl(final boolean commit) throws MalformedURLException, UnsupportedEncodingException {
		
		final StringBuilder sb = new StringBuilder(url.toString()).append("/update");
		if (commit) sb.append("?commit=true");
		return new URL(sb.toString());
		
	}
	
	/**
	 * Method to generate the final "select" URL according to the instance's state.
	 * @return
	 */
	public URL buildSelectUrl() throws MalformedURLException, UnsupportedEncodingException {
		
		final StringBuilder sb = new StringBuilder(url.toString()).append("/select/?indent=true");
		
		// search input text --> q=....
		if (StringUtils.hasText(input.getText())) {
			sb.append( "&q="+URLEncoder.encode(input.getText(), "UTF-8") );
		} else {
			sb.append( "&q="+URLEncoder.encode("*", "UTF-8") );
		}
		
		// search input type --> fq=type:Dataset
		if (StringUtils.hasText(input.getType())) {
			sb.append("&fq="+URLEncoder.encode( SolrXmlPars.FIELD_TYPE+":"+"\""+input.getType()+"\"","UTF-8" ));
		}
		
		// search input constraints --> fq=facet_name:"facet_value"
		final Map<String, List<String>> constraints = input.getConstraints();
		if (!constraints.isEmpty()) {
			for (final String facet : constraints.keySet()) {
				for (final String value : constraints.get(facet)) {
					// NOTE: include constraint values within quotes to execute exact string match
					sb.append("&fq="+URLEncoder.encode( facet+":"+"\""+value+"\"","UTF-8" ));
				}
			}
		}
		
		
		// &facet.field=...&facet.field=...
		if (this.facets!=null) {
			sb.append("&facet=true");
			for (final String facet : this.facets) {
				sb.append("&facet.field=").append( URLEncoder.encode(facet, UTF8 ));
			}
		}
		
		// &start=...@rows=...
		sb.append("&start=").append(input.getOffset())
		  .append("&rows=").append(input.getLimit());
		
		// indent=true
		//if (this.indent) {
		//	sb.append("indent=true");
		//}

		if (LOG.isInfoEnabled()) LOG.info("Select URL=" + sb.toString());
		return new URL(sb.toString());
		
	}
	

}
