package esg.search.publish.impl.solr;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.core.RecordSerializer;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.XmlParser;

/**
 * Implementation of {@link RecordSerializer} based on Solr XML.
 * 
 * @author Luca Cinquini
 *
 */
public class SolrRecordSerializer implements RecordSerializer {
    
    // The underlying XML parser
    static XmlParser parser = new XmlParser(false); // validate XSD = false
    
    /**
     * Method to build a Record object from a Solr/XML document.
     */
    public Record deserialize(String xml) throws IOException, JDOMException, NumberFormatException {
        
        // validate XML 
        // (throws JDOMEXception if XML is not valid)
        Document doc = parser.parseString(xml);
        Element root = doc.getRootElement();
        
        // create record stub
        Record record = new RecordImpl();
        
        // parse XML, populate record object
        for (Object field : root.getChildren(SolrXmlPars.ELEMENT_FIELD)) {
            
            String name = ((Element)field).getAttributeValue(SolrXmlPars.ATTRIBUTE_NAME);
            String value = ((Element)field).getTextNormalize();
            
            // special Record field "id"
            if (name.equals(QueryParameters.FIELD_ID)) {
                record.setId(value);
                
            // special record field "version"
            } else if (name.equals(QueryParameters.FIELD_VERSION)) {
                record.setVersion(Long.parseLong(value));
                
            // all other fields
            } else {
                record.addField(name, value);
            }
            
        }
        
        return record;
    }
    
    public String serialize(Record record) {
        return null;
    }

}
