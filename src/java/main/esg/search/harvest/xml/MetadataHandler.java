package esg.search.harvest.xml;

import java.util.List;

import org.jdom.Element;

import esg.search.core.Record;

/**
 * API for harvesting XML metadata documents conforming to different schemas.
 */
public interface MetadataHandler {
	
	/**
	 * Method to harvest a single XML document into one or more search records.
	 * @param root : the document top-level element.
	 * @return
	 */
	List<Record> parse(Element root);

}
