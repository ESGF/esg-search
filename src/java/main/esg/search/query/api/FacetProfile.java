package esg.search.query.api;

import java.util.Map;

/**
 * Interface representing a set of top-level search facets customized to a specific application or domain.
 */
public interface FacetProfile {
	
	/**
	 * Method to return an ordered list of top-level facets to be used by the application, indexed by key.
	 * @return
	 */
	Map<String, Facet> getTopLevelFacets();

}