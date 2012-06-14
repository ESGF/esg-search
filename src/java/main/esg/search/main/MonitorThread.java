package esg.search.main;

import java.net.URL;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;

public class MonitorThread extends Thread {
    
    private final XPath xPath1;
    private final XPath xPath2;
    private final XmlParser xmlParser;
    private final HttpClient httpClient;
   
    String url;
    String postdata = null;
    String response = null;
    long elapsedTime = -1;
    long queryTime = -1;
    int numFound = -1;
    
    /**
     * Constructor for GET request.
     * 
     * @param url
     * @throws Exception
     */
    public MonitorThread(final String url) throws Exception {
        
        this.url = url;
                
        this.xmlParser = new XmlParser(false);
        this.xPath1 = XPath.newInstance(MonitorManager.XPATH1);
        this.xPath2 = XPath.newInstance(MonitorManager.XPATH2);
        httpClient = new HttpClient();
        httpClient.setConnectionTimeout(MonitorManager.CONNECTION_TIMEOUT);
        httpClient.setReadTimeout(MonitorManager.READ_TIMEOUT);

    }
    
    /**
     * Constructor for POST request.
     * 
     * @param url
     * @param data
     * @throws Exception
     */
    public MonitorThread(final String url, String postdata) throws Exception {
        
        this(url);
        this.postdata = postdata;
        
    }

    public void run() {
        
        System.out.println("Querying URL="+url);
        try {
            
            // execute HTTP request
            long startTime = System.currentTimeMillis();
            // GET
            if (postdata==null) {
                this.response = httpClient.doGet(new URL(url));
            // POST
            } else {
                this.response = httpClient.doPost(new URL(url), postdata, false);
            }
            this.elapsedTime = System.currentTimeMillis() - startTime;
  
            // <int name="QTime">1</int>
            final Document doc = xmlParser.parseString(this.response);
            Element qTimeElement = (Element)xPath1.selectSingleNode(doc);
            this.queryTime = Integer.parseInt(qTimeElement.getTextNormalize());
            
            // <result name="response" numFound="9" start="0" maxScore="1.0">
            Element resultElement = (Element)xPath2.selectSingleNode(doc);
            this.numFound = Integer.parseInt( resultElement.getAttributeValue("numFound") );
            
        } catch(Exception e) {
            System.out.println("ERROR FROM URL "+url+":"+e.getMessage());
        }
        
    }
    
    public void print() {
        System.out.println("URL="+this.url+" Query Time="+this.queryTime+" Elapsed Time="+this.elapsedTime+" Number of Results="+this.numFound);
    }
        
}
