package esg.search.harvest.impl.solr;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.core.RecordSerializerSolrImpl;
import esg.search.harvest.api.RecordConsumer;
import esg.search.query.impl.solr.SolrUrlBuilder;
import esg.search.utils.HttpClient;

/**
 * Implementation of {@link RecordConsumer} that posts the record 
 * to a remote Solr server for indexing.
 */
public class SolrIndexer implements RecordConsumer {
	
	/**
	 * The base URL of the Solr server.
	 */
	private final URL url;
			
	/**
	 * Utility class used to serialize records into XML.
	 */
	private RecordSerializer serializer = new RecordSerializerSolrImpl();
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Instance attribute shared among all HTTP request,
	 * since the generation of the POST URL does not depend on the instance's state.
	 */
	private SolrUrlBuilder solrUrlBuilder;
	
	/**
	 * Client used to execute HTTP/POST requests.
	 */
	private HttpClient httpClient = new HttpClient();
	
	public SolrIndexer(final URL url) {
		
		this.url = url;
		solrUrlBuilder = new SolrUrlBuilder(this.url);
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void consume(final Record record) throws Exception {
		
		final String xml = serializer.serialize(record, true);
		final URL postUrl = solrUrlBuilder.buildUpdateUrl(true); // commit=true
		if (LOG.isDebugEnabled()) LOG.debug("Posting record:"+xml+" to URL:"+postUrl.toString());
		httpClient.doPostXml(postUrl, xml);
		
	}

}
