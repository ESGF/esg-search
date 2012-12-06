package esg.search.publish.validation;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.utils.XmlParser;

public class CoreRecordValidator implements RecordValidator {
    
    // The underlying XML parser
    XmlParser parser = new XmlParser(false); // validate XSD = false
    
    
    private final XPath xPath;
    
    public CoreRecordValidator() throws Exception {
        
        xPath = XPath.newInstance("/doc/field[@name='type']");
    }

    @Override
    public String validate(String record) throws Exception {
        
        Document doc = parser.parseString(record);
        Element typeElement = (Element)xPath.selectSingleNode(doc);
        if (typeElement==null) {
            throw new Exception("Unknown record type");
        }
        
        return typeElement.getTextNormalize();
    }

}
