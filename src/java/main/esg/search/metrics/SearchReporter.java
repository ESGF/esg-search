package esg.search.metrics;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.utils.HttpClient;
import esg.search.utils.XmlParser;

public class SearchReporter extends AbstractReporter {
        
    public final static String XPATH = "/response/result";
    private final XPath xpath;
    
    String[] facets;
    
    // Example: XPATH_TEMPLATE = "/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='project']";
    public final static String XPATH_TEMPLATE = "/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='FACET']";
    private final XPath[] xpaths;
    
    // Example: url = "http://INDEX_NODE_HOSTNAME/esg-search/search?latest=true&replica=false&facets=model,project";
    private String url = "http://INDEX_NODE_HOSTNAME/esg-search/search?latest=true&replica=false&project=CMIP5&facets=FACETS";
    
    private String cvs_file_path = "/esg/content/metrics/search_INDEX_NODE_HOSTNAME_FACETS.csv";
    private String xml_file_path = "/esg/content/metrics/search_INDEX_NODE_HOSTNAME_FACETS.xml";
    
    private final static String REPORT_TYPE = "Search Results";
    
    public SearchReporter(String hostname, String[] facets) throws Exception {
        
        url = url.replaceAll("INDEX_NODE_HOSTNAME", hostname)
                 .replaceAll("FACETS", StringUtils.join(facets, ","));
        
        cvs_file_path = cvs_file_path.replaceAll("INDEX_NODE_HOSTNAME", hostname)
                                     .replaceAll("FACETS", StringUtils.join(facets, "_"));
        xml_file_path = xml_file_path.replaceAll("INDEX_NODE_HOSTNAME", hostname)
                                     .replaceAll("FACETS", StringUtils.join(facets, "_"));
        
        xpath = XPath.newInstance(XPATH);

        this.facets = facets;
        xpaths = new XPath[facets.length];
        for (int i=0; i<facets.length; i++) {
            xpaths[i] = XPath.newInstance( XPATH_TEMPLATE.replaceAll("FACET", facets[i]) );
        }
        
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        if (args.length<=1) {
            System.out.println("Usage: java esg.search.metrics <index.node.hostname> <facet_1> <facet_2> <facet_3>....");
            System.exit(-1);
        }
        String indexNode = args[0];
        String[] facets = new String[args.length-1];
        for (int i=0; i<args.length-1; i++) facets[i] = args[i+1];
        
        SearchReporter self = new SearchReporter(indexNode, facets);
        self.run();

    }
    
    @Override
    public Map<String, Integer> report() throws Exception {

        // execute HTTP request
        System.out.println("URL="+this.url);
        HttpClient httpClient = new HttpClient();
        String xml = httpClient.doGet(new URL(this.url));

        // parse HTTP response
        XmlParser parser = new XmlParser(false);
        Document doc = parser.parseString(xml);
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        // <result name="response" numFound="9" start="0" maxScore="1.0">
        Element resultElement = (Element)xpath.selectSingleNode(doc);
        int total = Integer.parseInt( resultElement.getAttributeValue("numFound") );
        map.put("Total", total);
        
        //  <lst name="model">
        //      <int name="CCSM4">2237</int>
        //      <int name="CESM1(BGC)">188</int>
        for (int i=0; i<facets.length; i++) {
            Element facetElement = (Element)xpaths[i].selectSingleNode(doc);
            for (Object obj : facetElement.getChildren("int")) {
                Element elem = (Element)obj;
                String name = elem.getAttributeValue("name");
                int counts = Integer.parseInt(elem.getTextNormalize());
                map.put(facets[i]+"="+name, counts);
                System.out.println(facets[i]+"="+name+" counts="+counts);
            }
        }

        return map;

    }
    
    @Override
    public String getCsvFilePath() {
        return this.cvs_file_path;
    }

    @Override
    public String getXmlFilePath() {
        return this.xml_file_path;
    }

    @Override
    public String getReportType() {
        return REPORT_TYPE;
    }

}
