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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import esg.search.query.impl.solr.SolrUrlBuilder;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.HttpClient;

/**
 * Client object that sends XML requests to a Solr server.
 */
public class SolrClient {
					
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
	public SolrClient(final URL url) {
		
		solrUrlBuilder = new SolrUrlBuilder(url);
		
	}
	
	/**
     * Method to index a single XML record.
     * @param xml.
     * @param type : chosen among the supported record types.
     * @param commit : true to commit the transaction after indexing this record, false if other records are coming.
     * @return
     * @throws Exception
     */
    public String index(final String xml, final String type, boolean commit) throws Exception {
        
        // validate record type versus supported Solr cores
        final String core = SolrXmlPars.CORES.get( WordUtils.capitalize(type) );
        if (!StringUtils.hasText(core)) throw new Exception("Unmapped core for record type="+type);
        final URL postUrl = solrUrlBuilder.buildUpdateUrl(core);
        
        // send POST request
        if (LOG.isDebugEnabled()) LOG.debug("Posting record:"+xml+" to URL:"+postUrl.toString());
        String response = httpClient.doPost(postUrl, xml, true);
        
        // commit changes, do not optimize for a single record
        if (commit) this.commit();
        
        return response;
        
    }
    
    /**
     * Method to delete a list of documents, from all cores.
     * @param ids
     */
    public void delete(List<String> ids) throws Exception {
        
        // loop over all cores, remove records from all cores alike
        for (final String core : SolrXmlPars.CORES.values()) {
            final String xml = SolrXmlBuilder.buildDeleteMessage(ids, true);
            final URL postUrl = solrUrlBuilder.buildUpdateUrl(core); 
            if (LOG.isDebugEnabled()) LOG.debug("Posting record:"+xml+" to URL:"+postUrl.toString());
            httpClient.doPost(postUrl, xml, true);
        }
        
        // commit changes to all cores
        commit();
    }
	
	/**
	 * Method to commit changes to all cores,
	 * and wait till the commit goes into effect
	 */
	public void commit() throws MalformedURLException, UnsupportedEncodingException, IOException  {
	    
        for (final String core : SolrXmlPars.CORES.values()) {
            
            String xml = SolrXmlBuilder.buildCommitMessage();
            URL postUrl = solrUrlBuilder.buildUpdateUrl(core);
            if (LOG.isInfoEnabled()) LOG.info("Issuing commit:"+xml+" to URL:"+postUrl.toString());
            httpClient.doPost(postUrl, xml, true);
            
            // optimize index ?
            //if (optimize) {
            //    xml = messageBuilder.buildOptimizeMessage();
            //    if (LOG.isInfoEnabled()) LOG.info("Issuing optimize:"+xml+" to URL:"+postUrl.toString());
            //    httpClient.doPost(postUrl, xml, true);
            //}

        }
	}


}
