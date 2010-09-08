package esg.search.core;

import java.util.List;
import java.util.Map;

/**
 * Interface representing a search record,
 * both as item to be indexed into the search repository,
 * and to be returned as a search result.
 */
public interface Record {
	
	/**
	 * Method to return the record's unique identifier.
	 * @return
	 */
	public String getId();

	/**
	 * Method to assign the record's unique identifier;
	 */
	public void setId(String id);

	/**
	 * Method to add a field (name, value) pair to the record.
	 * @param name
	 * @param value
	 */
	public void addField(final String name, final String value);
	
	/**
	 * Method to return an (unmodifiable) map of multi-valued fields for this record.
	 * @return
	 */
	public Map<String, List<String>> getFields();

}