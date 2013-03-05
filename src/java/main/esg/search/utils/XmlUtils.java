package esg.search.utils;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Class containing general purpose XML utilities.
 * 
 * @author Luca Cinquini
 *
 */
public class XmlUtils {
    
    private final static String NEWLINE  = System.getProperty("line.separator");
    
    /**
     * Method to serialize an XML element to string.
     * 
     * @param element
     * @param indent
     * @return
     */
    public final static String toString(final Element element, final boolean indent) {
        Format format = (indent ? Format.getPrettyFormat() : Format.getCompactFormat());
        XMLOutputter outputter = new XMLOutputter(format);
        return outputter.outputString(element) + (indent ? NEWLINE : "");
    }
    
    private XmlUtils() {};

}
