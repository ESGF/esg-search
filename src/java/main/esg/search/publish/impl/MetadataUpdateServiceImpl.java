package esg.search.publish.impl;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.publish.api.MetadataUpdateService;
import esg.search.query.api.QueryParameters;
import esg.search.utils.HttpClient;
import esg.search.utils.Serializer;
import esg.search.utils.XmlParser;

public class MetadataUpdateServiceImpl implements MetadataUpdateService {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	private final static String XPATH1 = "/response/result";
	private final static String XPATH2 = "/response/result/doc/str[@name='id']";
	private final static int LIMIT = 100;
	
	private XPath xPath1;
	private XPath xPath2;
	
	public MetadataUpdateServiceImpl() throws Exception {
		xPath1 = XPath.newInstance(XPATH1);
		xPath2 = XPath.newInstance(XPATH2);
	}

	public void update(String url, String core, String action, HashMap<String, Map<String,String[]>> doc) throws Exception {

		HttpClient httpClient = new HttpClient();
		XmlParser xmlParser = new XmlParser(false);
		
		LOG.debug("Metadata update: url="+url+" action="+action);
		
		// process each query separately
		for (String query : doc.keySet()) {
		
			Map<String,String[]> metadata = doc.get(query);
			LOG.debug("Processing query: "+query);
			String[] constraints = query.split("&");
			
	        // VERY IMPORTANT: MUST FIRST CREATE ALL THE UPDATE DOCUMENTS, 
	        // THEN SENDING THEM WITH A commit=True STATEMENT
	        // OTHERWISE PAGINATION OF RESULTS DOES NOT WORK
			List<String> xmlDocs = new ArrayList<String>();
			
			// query ALL matching records
			int start = 0;
			int numFound = start + 1;
			while (start < numFound) {
								
				// build query URL
				String _url = url + "/" + core + "/select?"
				            + "q="+URLEncoder.encode("*:*","UTF-8")
				            + "&fl=id"
				            + "&wt=xml"
				            + "&indent=true"
				            + "&start="+start
				            + "&rows="+LIMIT;
				for (String constraint : constraints) {
					String[] kv = constraint.split("=");
					_url += "&fq="+kv[0]+":"+URLEncoder.encode(kv[1],"UTF-8"); // must URL-encode the values
				}
				
				// execute HTTP query request
				LOG.debug("HTTP request: "+_url);
			    String response = httpClient.doGet(new URL(_url));
			    LOG.debug("HTTP respose:" +response);
			    
			    // parse HTTP query response
			    final Document xmlDoc = xmlParser.parseString(response);
			    
			    // total number of results
	            // <result name="response" numFound="9" start="0" maxScore="1.0">
	            Element resultElement = (Element)xPath1.selectSingleNode(xmlDoc);
	            numFound = Integer.parseInt( resultElement.getAttributeValue("numFound") );
	            
	            // build the XML update document
	            /**
	             <?xml version="1.0" encoding="UTF-8" standalone="no"?>
				 <add>
				 	<doc>...</doc>
				 	<doc>...</doc>
				 	..............
				 </add>
	             */
	            Element _addElement = new Element("add");
	            List<Element> _docElements = _buildUpdateDocuments(xmlDoc, action, metadata);
	            _addElement.addContent(_docElements);
	            Document _jdoc = new Document(_addElement);
				xmlDocs.add( Serializer.JDOMtoString(_jdoc) );
				
				// increment counter for next request
				start += _docElements.size();
				
			} // loop over multiple HTTP query request/response
			
			// send all updates, commit each time
			String solrUrl = "http://esgf-dev.jpl.nasa.gov:8984/solr/datasets/update?commit=true"; // FIXME
			for (String xmlDoc : xmlDocs) {
				LOG.debug(xmlDoc);
				httpClient.doPost(new URL(solrUrl), xmlDoc, true); // xml=true
			}
					
		} // loop over queries
		
	}
	
    // Method to build an XML update document snippet
    //	<doc>
    //		<field name="id">cmip5.output1.CSIRO-BOM.ACCESS1-3.historical.mon.ocean.Omon.r1i1p1.v2|aims3.llnl.gov</field>
    //		<field name="xlink" update="add">ccc</field>
    //	</doc>
	private List<Element> _buildUpdateDocuments(Document xmlDoc, String action, Map<String,String[]> metadata) throws Exception {
        
		List<Element> _docElements = new ArrayList<Element>();
		
        // loop over results across this response
        for (Object obj : xPath2.selectNodes(xmlDoc)) {
        	
        	//start += 1; // will start from next record
        	Element idElement = (Element)obj;
        	String id = idElement.getText();
        	
        	Element _docElement = new Element("doc");
        	Element _idElement = new Element("field");
        	_idElement.setAttribute("name", "id");
        	_idElement.setText(id);
        	_docElement.addContent(_idElement);
        	
        	// loop over metadata keys to set/add/remove
        	for (String key : metadata.keySet()) {
        		String[] values = metadata.get(key);
        		
        		if (values.length>0) {
        		
            		// set all new values
            		for (String value : values) {	
            			Element _fieldElement = new Element("field");
            			_fieldElement.setAttribute("name", key);
            			_fieldElement.setAttribute("update", action.toLowerCase());
            			_fieldElement.setText(value);
            			_docElement.addContent(_fieldElement);
            		}
            		
        		} else {
        			
        			// remove all values
        			// <field name="xlink" update="set" null="true"/>
        			Element _fieldElement = new Element("field");
        			_fieldElement.setAttribute("name", key);
        			_fieldElement.setAttribute("update", action.toLowerCase());
        			_fieldElement.setAttribute("null", "true");
        			_docElement.addContent(_fieldElement);

        		}
        		
        	}
        	
        	_docElements.add(_docElement);
         	
        } // loop over results within one HTTP response		
        
        return _docElements;

	}
	
	/**
	 * Command line invocation method.
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		// FIXME: parse arguments from command line
		String url = "http://esgf-dev.jpl.nasa.gov:8984/solr";
		String core = "datasets";
		
		HashMap<String, Map<String,String[]>> doc = new HashMap<String, Map<String,String[]>>();
		Map<String,String[]> metadata = new HashMap<String,String[]>();
		String query = "id=cmip5.output1.CSIRO-BOM.ACCESS1-3.historical.mon.ocean.Omon.r1i1p1.v2|aims3.llnl.gov";
		//String query = "variable=bacc&type=Dataset";
		//String query = "variable=areacella&type=Dataset";
		doc.put(query, metadata);

		/** SET example
		<?xml version="1.0" encoding="UTF-8"?>
		 <add>
		 	<doc>
				<field name="id">cmip5.output1.CSIRO-BOM.ACCESS1-3.historical.mon.ocean.Omon.r1i1p1.v2|aims3.llnl.gov</field>
				<field name="xlink" update="set">abc</field>
				<field name="xlink" update="set">cde</field>
			</doc>
			</add>
		 */
		String action = QueryParameters.ACTION_SET;
		metadata.put("xlink", new String[] {"abc", "cde"} );
		
		/** ADD example
		 <?xml version="1.0" encoding="UTF-8"?>
			<add>
				<doc>
					<field name="id">cmip5.output1.CSIRO-BOM.ACCESS1-3.historical.mon.ocean.Omon.r1i1p1.v2|aims3.llnl.gov</field>
					<field name="xlink" update="add">123</field>
					<field name="xlink" update="add">456</field>
				</doc>
			</add>
		 */
		//String action = QueryParameters.ACTION_ADD;
		//metadata.put("xlink", new String[] {"123", "456"} );
		
		/** REMOVE example (remove specified values - only once per value!)
		 <?xml version="1.0" encoding="UTF-8"?>
			<add>
				<doc>
					<field name="id">cmip5.output1.CSIRO-BOM.ACCESS1-3.historical.mon.ocean.Omon.r1i1p1.v2|aims3.llnl.gov</field>
					<field name="xlink" update="remove">123</field>
					<field name="xlink" update="remove">456</field>
				</doc>
			</add>
		 */
		//String action = QueryParameters.ACTION_REMOVE;
		//metadata.put("xlink", new String[] {"123", "456"} );

		/** SET null="true" EXAMPLE (remove all values)
		   <?xml version="1.0" encoding="UTF-8"?>
			<add>
				<doc>
					<field name="id">cmip5.output1.CSIRO-BOM.ACCESS1-3.historical.mon.ocean.Omon.r1i1p1.v2|aims3.llnl.gov</field>
					<field name="xlink" update="set" null="true" />
				</doc>
			</add>
		 */
		//String action = QueryParameters.ACTION_SET;
		//metadata.put("xlink", new String[] {} );
		
		MetadataUpdateServiceImpl self = new MetadataUpdateServiceImpl();
		self.update(url, core, action, doc);
		
	}

}
