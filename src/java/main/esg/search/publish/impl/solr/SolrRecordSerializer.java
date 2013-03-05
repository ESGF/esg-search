package esg.search.publish.impl.solr;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.core.RecordSerializer;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.XmlParser;
import esg.search.utils.XmlUtils;

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
    public Record deserialize(String xml) throws IOException, JDOMException, NumberFormatException, URISyntaxException {
        
        // validate XML 
        // (throws JDOMEXception if XML is not valid)
        Document doc = parser.parseString(xml);
        Element root = doc.getRootElement();
        
        // create record stub
        Record record = new RecordImpl();
        
        // optional record schema (note: convert to lower case)
        String schema = root.getAttributeValue(SolrXmlPars.ATTRIBUTE_SCHEMA);
        if (StringUtils.hasText(schema)) record.setSchema(new URI(schema.toLowerCase()));
        
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
    
    /**
     * Method to build an Solr/XML document from a record object.
     */
    public Element serialize(Record record) {
        
        // <doc>
        final Element docEl = new Element(SolrXmlPars.ELEMENT_DOC);
        
        // <doc schema="...">
        if (record.getSchema()!=null) {
            docEl.setAttribute(SolrXmlPars.ATTRIBUTE_SCHEMA, record.getSchema().toString());
        }
        
        // <field name="id">...</field>
        final Element idEl = new Element(SolrXmlPars.ELEMENT_FIELD);
        idEl.setAttribute(SolrXmlPars.ATTRIBUTE_NAME, QueryParameters.FIELD_ID);
        idEl.setText(record.getId());
        docEl.addContent(idEl);
        
        // <field name="version">...</field>
        if (record.getVersion()!=0) {
            final Element vEl = new Element(SolrXmlPars.ELEMENT_FIELD);
            vEl.setAttribute(SolrXmlPars.ATTRIBUTE_NAME, QueryParameters.FIELD_VERSION);
            vEl.setText(Long.toString(record.getVersion()));
            docEl.addContent(vEl);
        }
        
        // <field name="...">....</field>
        // (for each value)
        final Map<String, List<String>> fields = record.getFields();
        for (final String key : fields.keySet()) {
            for (final String value : fields.get(key)) {
                final Element fieldEl = new Element(SolrXmlPars.ELEMENT_FIELD);
                fieldEl.setAttribute(SolrXmlPars.ATTRIBUTE_NAME, key);
                fieldEl.setText(value);
                docEl.addContent(fieldEl);
            }
        }
        
        return docEl;
    }
    
    /**
     * Method to build an Solr/XML document from a record object.
     */
    public String serialize(Record record, boolean indent) {
        
        Element docEl = this.serialize(record);
        return XmlUtils.toString(docEl, indent);        
        
    }
   
}
