package esg.search.utils;

import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Utility class to handle XML documents.
 */
public class XmlParser {
	
	/**
	 * The underlying XML parser.
	 */
	private final SAXBuilder builder;

	/**
	 * Constructor instantiates the underlying XML parser.
	 */
	public XmlParser(final boolean validate) {
		builder = new SAXBuilder(); 
		builder.setValidation(validate); 
		builder.setIgnoringElementContentWhitespace(true); 
	}
	
	/**
	 * Method to parse an XML string into a JDOM document.
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public Document parseString(final String xml) throws IOException, JDOMException {
		final StringReader sr = new StringReader(xml);
		return builder.build(sr); 
	}

	/**
	 * Method to parse an XML file into a JDOM document.
	 * @param filepath
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public Document parseFile(final String filepath) throws IOException, JDOMException {
		return builder.build(filepath);
	}

}
