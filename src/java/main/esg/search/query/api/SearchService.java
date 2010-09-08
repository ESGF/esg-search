package esg.search.query.api;

import java.util.Map;

/**
 * Interface defining the API for mix faceted/text search.
 */
public interface SearchService {
		
	/**
	 * Method to return the search facets, indexed by facet key, subject to the given query constraints.
	 * @return
	 */
	public Map<String, Facet> getFacets(final SearchInput input) throws Exception;
	
	/**
	 * Method to return a list of results matching the given query constraints.
	 * @return
	 * @throws Exception
	 */
	public SearchOutput getResults(final SearchInput input) throws Exception;

}
