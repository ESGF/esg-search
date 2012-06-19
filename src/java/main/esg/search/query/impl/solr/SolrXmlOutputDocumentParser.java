package esg.search.query.impl.solr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.springframework.util.StringUtils;

import esg.search.core.RecordHelper;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;
import esg.search.query.ws.rest.OutputDocumentParser;
import esg.search.query.ws.rest.WgetScriptGenerator;
import esg.search.utils.XmlParser;

/**
 * Implementation of {@link OutputDocumentParser} that acts on response documents
 * sent by a Solr server.
 * 
 * @author Luca Cinquini
 *
 */
public class SolrXmlOutputDocumentParser implements OutputDocumentParser {
    
    // Xpath expression for locating result records inside the response document
    private static XPath DOCXPATH;
    
    static {
        try {
            DOCXPATH = XPath.newInstance("/response/result/doc");
        } catch(Exception e) {
            
        }
    }

    @Override
    public Map<String, List<String>> extractDatasets(String xml) throws Exception {
        
        final Map<String, List<String>> datasets = new LinkedHashMap<String, List<String>>();
        
        // parse the Solr/XML document
        final XmlParser xmlParser = new XmlParser(false);
        final Document doc = xmlParser.parseString(xml);
        //XPath xpath = XPath.newInstance("/response/result/doc/arr[@name='url']/str");
        
        // loop over records
        for (Object obj : DOCXPATH.selectNodes(doc)) {
            Element docEl = (Element)obj;
            
            String id = "";
            String index_node = "";
            
            // loop over record subelements
            for (final Object childObj : docEl.getChildren("str")) {
                
                Element childEl = (Element)childObj;
                
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_ID)) {
                    id = childEl.getTextNormalize();
                }
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_INDEX_NODE)) {
                    index_node = childEl.getTextNormalize();
                }
            }

            // store id, index_node for this record
            if (StringUtils.hasText(id) && StringUtils.hasText(index_node)) {
                if (!datasets.containsKey(index_node)) {
                    datasets.put(index_node, new ArrayList<String>());
                }
                datasets.get(index_node).add(id);
            }

        }
        
        return datasets;
        
    }

    @Override
    public int extractFiles(String xml, WgetScriptGenerator.WgetDescriptor desc) throws Exception {
        
        int numFiles = 0;
        
        // parse the Solr/XML document
        final XmlParser xmlParser = new XmlParser(false);
        final Document doc = xmlParser.parseString(xml);
        
        // loop over records
        for (Object obj : DOCXPATH.selectNodes(doc)) {
            Element docEl = (Element)obj;
                        
            String url = ""; // Note: only extract one URL for each record
            String checksum = "";
            String checksumType = "";

            for (final Object childObj : docEl.getChildren("arr")) {
                
                Element childEl = (Element)childObj;
                
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_CHECKSUM)) {
                    checksum = childEl.getChild("str").getTextNormalize();
                }
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_CHECKSUM_TYPE)) {
                    checksumType = childEl.getChild("str").getTextNormalize();
                }
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_URL)) {
                    String tuple = childEl.getChild("str").getTextNormalize();
                    String[] parts = RecordHelper.decodeTuple(tuple);
                    if (parts[2].equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_HTTP)) {
                        url = parts[0];
                    }
                }
             }
            desc.addFile(url, null, null, checksumType, checksum);
            numFiles ++;
             
        }       
        
        return numFiles;
        
    }

}
