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
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import esg.search.core.Record;
import esg.search.publish.api.RecordConsumer;

/**
 * Implementation of {@link RecordConsumer} that sends (fully populated) records to a Solr server for indexing.
 */
@Component("indexer")
public class SolrIndexer implements RecordConsumer {
    
    // client object that sends XML requests to Solr server
    final SolrClient solrClient;
				
	/**
	 * Constructor delegates to superclass.
	 * @param url
	 */
	@Autowired
	public SolrIndexer(final @Value("${esg.search.solr.publish.url}") URL url) {
	    solrClient = new SolrClient(url);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public void consume(final Record record) throws Exception {
	    	    
		final String xml = SolrXmlBuilder.buildAddMessage(record, true);
		solrClient.index(xml, record.getType(), true); // commit=true
				
	}
		
	/**
     * {@inheritDoc}
     * 
     * Note that this implementation will first index all records,
     * then commit all changes at once.
     */
    public void consume(final Collection<Record> records) throws Exception {
        
        // index one record at a time, do not commit
        for (final Record record : records) {
            
            final String xml = SolrXmlBuilder.buildAddMessage(record, true);
            solrClient.index(xml, record.getType(), true);
            
        }
        
        // commit all records at once, to all cores
        solrClient.commit();
        
    }

}
