package esg.search.utils;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;

public class XmlTest {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        
        String inst = "/Users/cinquini/Documents/workspace-esg/xml/dataset2.xml";
        String xsd = "/Users/cinquini/Documents/workspace-esg/xml/esgf30.xsd";
        
        File xsdfile = new File(xsd);
        XMLReaderJDOMFactory schemafac = new XMLReaderXSDFactory(xsdfile);
        SAXBuilder builder = new SAXBuilder(schemafac);
        File xmlfile = new File(inst);
        Document validdoc = builder.build(xmlfile);
        System.out.println("ok"+validdoc);

    }

}
