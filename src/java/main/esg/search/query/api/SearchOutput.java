package esg.search.query.api;

import java.util.List;

import esg.search.core.Record;

/**
 * Interface representing the output of a search operation.
 */
public interface SearchOutput {
	
	/**
	 * Getter method for the total number of results found.
	 * @return
	 */
	public int getCounts();

	/**
	 * Setter method for the total number of results found.
	 * @param counts
	 */
	public void setCounts(int counts);
	
	/**
	 * Getter method for the offset into the returned results.
	 * @return
	 */
	public int getOffset();
	
	/**
	 * Setter method for the offset into the returned results.
	 * @return
	 */
	public void setOffset(int offset);
	
	/**
	 * Method to return the list of results.
	 * @return
	 */
	public List<Record> getResults();
	
	/**
	 * Method to add a single result to the list.
	 * @param record
	 */
	public void addResult(Record record) ;

}