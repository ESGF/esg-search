package esg.search.metrics;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;

public class SearchReporter extends AbstractReporter {
    
    //private final static String URL = "http://esg-datanode.jpl.nasa.gov/esg-search/search?latest=true&replica=false&facets=model,project&distrib=false";
    private final static String URL = "http://localhost/esg-search/search?latest=true&replica=false&facets=model,project&distrib=false";
    
    public final static String XPATH1 = "/response/result";
    private final XPath xpath1;
    
    public final static String XPATH2 = "/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='model']";
    private final XPath xpath2;
    
    public final static String XPATH3 = "/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='project']";
    private final XPath xpath3;
    
    private final static String CSV_FILE_PATH = "/esg/content/metrics/search.csv";
    private final static String XML_FILE_PATH = "/esg/content/metrics/search.xml";
    
    private final static String REPORT_TYPE = "Search Results";
    
    public SearchReporter() throws Exception {
        
        xpath1 = XPath.newInstance(XPATH1);
        xpath2 = XPath.newInstance(XPATH2);
        xpath3 = XPath.newInstance(XPATH3);
        
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        SearchReporter self = new SearchReporter();
        self.run();

    }
    
    @Override
    public Map<String, Integer> report() throws Exception {

        // execute HTTP request
        HttpClient httpClient = new HttpClient();
        String xml = httpClient.doGet(new URL(URL));

        // parse HTTP response
        XmlParser parser = new XmlParser(false);
        Document doc = parser.parseString(xml);
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        // <result name="response" numFound="9" start="0" maxScore="1.0">
        Element resultElement = (Element)xpath1.selectSingleNode(doc);
        int total = Integer.parseInt( resultElement.getAttributeValue("numFound") );
        map.put("Total", total);
        
        //  <lst name="model">
        //      <int name="CCSM4">2237</int>
        //      <int name="CESM1(BGC)">188</int>
        Element modelElement = (Element)xpath2.selectSingleNode(doc);
        for (Object obj : modelElement.getChildren("int")) {
            Element elem = (Element)obj;
            String name = elem.getAttributeValue("name");
            int counts = Integer.parseInt(elem.getTextNormalize());
            map.put("model="+name, counts);
        }
        
        //  <lst name="project">
        //      <int name="CMIP5">11195</int>
        //      <int name="GeoMIP">169</int>
        Element projectElement = (Element)xpath3.selectSingleNode(doc);
        for (Object obj : projectElement.getChildren("int")) {
            Element elem = (Element)obj;
            String name = elem.getAttributeValue("name");
            int counts = Integer.parseInt(elem.getTextNormalize());
            map.put("project="+name, counts);
        }

        return map;

    }
    
    @Override
    public String getCsvFilePath() {
        return CSV_FILE_PATH;
    }

    @Override
    public String getXmlFilePath() {
        return XML_FILE_PATH;
    }

    @Override
    public String getReportType() {
        return REPORT_TYPE;
    }

}