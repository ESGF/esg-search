package esg.search.query.ws.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.util.AbstractSolrTestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import esg.search.core.Record;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlParser;
import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;

/**
 * An extension of the SolrTestCase to be used together with the P2P case
 * @author Estani
 *
 */
public class P2PSolrTestCase extends AbstractSolrTestCase {
    
    // define the location of the Solr configuration files
    static {
        System.setProperty("solr.solr.home", 
                           System.getProperty("user.dir")+"/src/java/test/solr");
    }
    
    private final Log LOG = LogFactory.getLog(this.getClass());

    // reference to solr schema
    @Override
    public String getSchemaFile() {
        return System.getProperty("solr.solr.home") + "/conf/schema.xml";
    }

    // reference to solr configuration
    @Override
    public String getSolrConfigFile() {
        return System.getProperty("solr.solr.home") + "/conf/solrconfig.xml";
    }

    protected String defaultService = "http://pcmdi9.llnl.gov/esg-search/search?";
    
    /**
     * Queries a p2p index and ingest those records into the local Solr instance for testing purposes.
     * @param query querystring to query a remote p2p server
     * @throws MalformedURLException
     * @throws IOException
     * @throws JDOMException
     */
    protected void ingest(String query) throws MalformedURLException, IOException, JDOMException {
        ingest(null, query);
    }
    
    /**
     * Queries a p2p index and ingest those records into the local Solr instance for testing purposes.
     * @param searchService url to p2p search service (e.g. "http://pcmdi9.llnl.gov/esg-search/search?")
     * @param query querystring to query a remote p2p server
     * @throws MalformedURLException
     * @throws IOException
     * @throws JDOMException
     */
    protected void ingest(String searchService, String query) throws MalformedURLException, IOException, JDOMException {
        if (null == searchService) searchService = defaultService;
        
        HttpClient client = new HttpClient();
        String response = client.doGet(new URL(searchService + query));
        
        final XmlParser xmlParser = new XmlParser(false);
        final Document doc = xmlParser.parseString(response);
        final SolrXmlParser solrParser = new SolrXmlParser();
        XPath xpath = XPath.newInstance("/response/result/doc");
        int count = 0;
        for (Object obj : xpath.selectNodes(doc)) {
            final Record record = solrParser.parseDoc((Element)obj);
            final List<String> fields = toList(record);
            assertU( adoc(fields.toArray(new String[fields.size()])) );
            count++;
        }
        
        assertU(commit());

        LOG.info("Ingested " + count + " records.");
        
    }
    
    public void testme() {
        assert(true);
    }
    
    /**
     * Turned off as you need a working index for this, and no index is guaranteed to be working at this time.
     * @throws Exception
     */
    public void _testCheckIngestion() throws Exception {
        // test ingestion
        ingest("type=File&query=*&limit=3&distrib=false");
        assertQ("couldn't find anything!", req("*"), "//result[@numFound=3]");
        //clean up
        assertU(delQ("*"));
        assertU(commit());
        assertQ("couldn't delete the records!", req("*"), "//result[@numFound=0]");
    }
    
    
    
    /**
     * Method that returns a flattened search record as a list of (name, value) pairs.
     * @param record
     * @return
     */
    private List<String> toList(final Record record) {
        
        final List<String> fields = new ArrayList<String>();
        fields.add(QueryParameters.FIELD_ID);
        fields.add(record.getId());
        final Map<String, List<String>> map = record.getFields();
        for (final String key : map.keySet()) {
            for (final String value : map.get(key)) {
                fields.add(key);
                fields.add(value);
            }
        }
        return fields;
    }
}
