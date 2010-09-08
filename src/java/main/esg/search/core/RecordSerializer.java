package esg.search.core;

import org.jdom.Element;



/**
 * Interface to serialize/deserialize a search record into an XML document.
 */
public interface RecordSerializer {
	
	/**
	 * Method to serialize a search record into an XML <doc> snippet.
	 * @param record
	 * @param indent
	 * @return
	 */
	public String serialize(Record record, boolean indent);
	
	/**
	 * Method to deserialize an XML <doc> snippet into a search record object.
	 * @param xml
	 * @return
	 */
	public Record deserialize(final Element doc);

}
