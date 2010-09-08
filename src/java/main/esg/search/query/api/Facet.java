package esg.search.query.api;

import java.util.List;

/**
 * Interface representing a search category, 
 * i.e. an index space that can be used to constraint the search results.
 */
public interface Facet {
	
	/**
	 * Returns the facet key used for programmatic access (immutable).
	 * @return
	 */
	public String getKey();
	
	/**
	 * Returns the facet label exposed to human users (immutable).
	 * @return
	 */
	public String getLabel();
	
	/**
	 * Returns the optional facet description for human users (immutable).
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Returns the current count of records associated with this facet.
	 * @return
	 */
	public int getCounts();
	
	/**
	 * Method to set the current record count.
	 */
	public void setCounts(int counts);
	
	/**
	 * Returns a list of sub-facets for the current facet.
	 * @return
	 */
	public List<Facet> getSubFacets();
	
	/**
	 * Adds a sub-facet to the current facet.
	 * @param subFacet
	 */
	public void addSubFacet(Facet subFacet);

}
