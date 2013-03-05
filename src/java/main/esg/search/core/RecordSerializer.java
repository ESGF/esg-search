package esg.search.core;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * API to serialize/deserialize ESGF search records to/from Solr/XML.
 * 
 * @author Luca Cinquini
 *
 */
public interface RecordSerializer {
    
    /**
     * Method to build an XML document representing a Record object.
     * 
     * @param record: ESGF search Record object
     * @param indent: true to indent the XML elements, false to return a compact string.
     * @return: XML document as string
     */
    String serialize(Record record, boolean indent);
    
    /**
     * Method to build an XML document representing a Record object.
     * 
     * @param record: ESGF search Record object
     * @return: XML document as Element object.
     * @param record
     * @return
     */
    Element serialize(Record record);
    
    /**
     * Method to build a Record object from an XML document.
     * 
     * @param xml: XML document serialized as string
     * @return: Record object
     * @throws IOException
     * @throws JDOMException: in case of XML parsing error
     */
    Record deserialize(String xml) throws IOException, JDOMException, URISyntaxException;

}
