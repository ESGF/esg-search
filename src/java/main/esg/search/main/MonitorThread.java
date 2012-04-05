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
   
    String server;
    String core;
    long elapsedTime = -1;
    long queryTime = -1;
    int numFound = -1;
    
    public MonitorThread(final String server, final String core) throws Exception {
        
        this.server = server;
        this.core = core;
        
        this.xmlParser = new XmlParser(false);
        this.xPath1 = XPath.newInstance(MonitorManager.XPATH1);
        this.xPath2 = XPath.newInstance(MonitorManager.XPATH2);
        httpClient = new HttpClient();
        httpClient.setConnectionTimeout(MonitorManager.CONNECTION_TIMEOUT);
        httpClient.setReadTimeout(MonitorManager.READ_TIMEOUT);

    }

    public void run() {
        
        String url = MonitorManager.buildUrl(server, core);
        System.out.println("Querying URL="+url);
        try {
            
            // execute HTTP request
            long startTime = System.currentTimeMillis();
            String xml = httpClient.doGet(new URL(url));
            this.elapsedTime = System.currentTimeMillis() - startTime;
  
            // <int name="QTime">1</int>
            final Document doc = xmlParser.parseString(xml);
            Element qTimeElement = (Element)xPath1.selectSingleNode(doc);
            this.queryTime = Integer.parseInt(qTimeElement.getTextNormalize());
            
            // <result name="response" numFound="9" start="0" maxScore="1.0">
            Element resultElement = (Element)xPath2.selectSingleNode(doc);
            this.numFound = Integer.parseInt( resultElement.getAttributeValue("numFound") );
            
        } catch(Exception e) {
            System.out.println("ERROR:"+e.getMessage());
        }

        
    }
    
}
