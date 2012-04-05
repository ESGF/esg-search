package esg.search.query.impl.solr;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;


/**
 * Utility class to probe a single Solr shard in a separate thread of execution.
 * 
 * @author Luca Cinquini
 */
public class ShardProbe extends Thread {
    
    private String shard = null;
    private long elapsedTime = -1;
    private long queryTime = -1;
    private int numFound = -1;
    
    private final static String XPATH1 = "/response/lst[@name='responseHeader']/int[@name='QTime']";
    private final static String XPATH2 = "/response/result";
    private final static int CONNECTION_TIMEOUT = 1000;
    private final static int READ_TIMEOUT = 5000;
    private final static String QUERY = "q=*&replica=false&latest=true";
    
    private final Log LOG = LogFactory.getLog(this.getClass());

    public ShardProbe(String shard) {
        this.shard = shard;
    }
    
    /**
     * Thread method.
     */
    public void run() {
        
        final String url = this.buildUrl();
        if (LOG.isInfoEnabled()) LOG.info("Querying URL="+url);
        
        try {
            
            // execute HTTP request           
            final HttpClient httpClient = new HttpClient();
            httpClient.setConnectionTimeout(CONNECTION_TIMEOUT);
            httpClient.setReadTimeout(READ_TIMEOUT);
            long startTime = System.currentTimeMillis();
            String xml = httpClient.doGet(new URL(url));
            this.elapsedTime = System.currentTimeMillis() - startTime;
  
            // <int name="QTime">1</int>
            final XmlParser xmlParser = new XmlParser(false);
            final XPath xPath1 = XPath.newInstance(XPATH1);
            final Document doc = xmlParser.parseString(xml);
            final Element qTimeElement = (Element)xPath1.selectSingleNode(doc);
            this.queryTime = Integer.parseInt(qTimeElement.getTextNormalize());
            
            // <result name="response" numFound="9" start="0" maxScore="1.0">
            final XPath xPath2 = XPath.newInstance(XPATH2);
            final Element resultElement = (Element)xPath2.selectSingleNode(doc);
            this.numFound = Integer.parseInt( resultElement.getAttributeValue("numFound") );
            
            if (LOG.isInfoEnabled()) LOG.info("Number of Results="+this.numFound+" Query Time="+this.queryTime+" Elapsed Time="+this.elapsedTime);
            
        } catch(Exception e) {
            System.out.println("Error probing shard="+shard+" :"+e.getMessage());
        }

        
    }
    
    /**
     * Method to build the full core URL from the Solr shard.
     * @return
     */
    private String buildUrl() {
        return "http://"+ this.shard + "/datasets/select/?"+QUERY;
    }

    public String getShard() {
        return shard;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public int getNumFound() {
        return numFound;
    }

}
