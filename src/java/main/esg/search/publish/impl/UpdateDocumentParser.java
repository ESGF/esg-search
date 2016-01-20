package esg.search.publish.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import esg.search.publish.api.MetadataUpdateService;
import esg.search.utils.Serializer;
import esg.search.utils.XmlParser;

/**
 * Utility class to parse an ESGF XML document 
 * into the required parameters needed to invoke a {@link} MetadataUpdateService
 * 	
 	<updates action="set">
   		<update>
      		<query>id:test.test.v1.testData.nc|esgf-dev.jpl.nasa.gov</query>
      		<field name="xlink">
         		<value>abc</value>
         		<value>123</value>
      		</field>
   		</update>
	</updates>
 * 
 * @author Luca Cinquini
 *
 */
public class UpdateDocumentParser {
	
	private String action;
	private String core;
	private HashMap<String, Map<String,String[]>> doc = new HashMap<String, Map<String,String[]>>();
	
	public UpdateDocumentParser(String xmlString) throws Exception {
		
		// parse XML
		XmlParser xmlParser = new XmlParser(false);
		Document xmlDoc = xmlParser.parseString(xmlString);
		Element root = xmlDoc.getRootElement();
		
		// Solr core
		this.core = root.getAttributeValue("core");
		
		// action=set/add/remove
		this.action = root.getAttributeValue("action");
		
		// loop over separate updates
		for (Object updateObj : root.getChildren("update")) {
			
			
			Element updateEl = (Element)updateObj;
			Element queryEl = (Element)updateEl.getChild("query");
			String query = queryEl.getTextTrim();
	
			Map<String,String[]> metadata = new HashMap<String,String[]>();
			for (Object fieldObj : updateEl.getChildren("field")) {
				
				Element fieldEl = (Element)fieldObj;
				String fieldName = fieldEl.getAttributeValue("name");
				List<String> fieldValues = new ArrayList<String>();
				
				for (Object valueObj : fieldEl.getChildren("value")) {
					Element valueEl = (Element)valueObj;
					fieldValues.add(valueEl.getTextTrim());
				}
				
				metadata.put(fieldName, fieldValues.toArray(new String[]{}));
				
			}
			
			this.doc.put(query, metadata);
			
		}
		
	}
	
	public String getAction() {
		return action;
	}
	
	public String getCore() {
		return core;
	}

	public HashMap<String, Map<String, String[]>> getDoc() {
		return doc;
	}
	
	/**
	 * Debug method: 
	 * parses an existing XML update document
	 * then sends it to the master Solr
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		File file = new File("/Users/cinquini/tmp/update_record_set.xml");
		XmlParser xmlParser = new XmlParser(false);
		Document xmlDoc = xmlParser.parseFile(file);
		String xmlString = Serializer.JDOMtoString(xmlDoc);

		UpdateDocumentParser parser = new UpdateDocumentParser(xmlString);
		String action = parser.getAction();
		String core = parser.getCore();
		HashMap<String, Map<String,String[]>> doc = parser.getDoc();
		
		String queryUri = "http://esgf-dev.jpl.nasa.gov:8984/solr";
		
		// execute update
		MetadataUpdateService updateService = new MetadataUpdateServiceImpl();
		updateService.update(queryUri, core, action, doc);

	}
	
}
