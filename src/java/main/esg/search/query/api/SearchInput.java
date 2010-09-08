package esg.search.query.api;

import java.util.List;
import java.util.Map;

/**
 * Interface representing the input criteria for a mixed facet/text query,
 * as well as the requested output (result type and applicable facets).
 */
public interface SearchInput {
	
	/**
	 * Setter method for the free text to search for.
	 * @param text
	 */
	public void setText(String text);
	
	/**
	 * Getter method for the free text to search for.
	 * @return
	 */
	public String getText();
	
	/**
	 * Getter method for the results type to search for.
	 * @return
	 */
	public String getType();


	/**
	 * Setter method for the results type to search for.
	 * @param type
	 */
	public void setType(String type);
	
	/**
	 * Method to add a search constraint as a (name,value) pair
	 * (to any values already existing for the named constraint).
	 * @param name
	 * @param values
	 */
	public void addConstraint(String name, String value);
	
	/**
	 * Method to remove a search constraint
	 * (i.e. all values set for this named constraint).
	 * @param name
	 */
	public void removeConstraint(String name);
	
	/**
	 * Method to set a multi-valued search constraint
	 * (overrides any already existing values for that named constraint).
	 * @param name
	 * @param values
	 */
	public void addConstraints(String name, List<String> values);
	
	/**
	 * Method to retrieve the query constraints as (name, values) pairs.
	 * @return
	 */
	public Map<String, List<String>> getConstraints();
	
	/**
	 * Setter method for the index of the first result to be returned.
	 * @param offset
	 */
	public void setOffset(int offset);
	
	/**
	 * Getter method for the index of the first result to be returned.
	 * @return
	 */
	public int getOffset();
	
	/**
	 * Setter method for the maximum number of results to be returned.
	 * @param limit
	 */
	public void setLimit(int limit);
	
	/**
	 * Getter method for the maximum number of results to be returned.
	 * @return
	 */
	public int getLimit();
	
	/**
	 * Method to request that a given facet be included in the search output.
	 * @param facet
	 */
	public void addFacet(String facet);
	
	/**
	 * Getter method for the ordered list of facets to be returned in the search output.
	 * @return
	 */
	public List<String> getFacets();
	
	/**
	 * Setter method for the ordered list of facets to be returned in the search output.
	 * @param facets
	 */
	public void setFacets(List<String> facets);

}
