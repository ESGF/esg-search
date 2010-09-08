package esg.search.query.impl.solr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import esg.search.query.api.Facet;
import esg.search.query.api.FacetProfile;

/**
 * Base implementation of {@link FacetProfile} initialized from a map of (facet key, facet label) pairs.
 */
public class FacetProfileImpl implements FacetProfile {
	
	private Map<String, Facet> facets = new LinkedHashMap<String, Facet>();

	/**
	 * Constructor builds the list of facets from a configuration map composed of (facet key, facet label) pairs.
	 * @param facets
	 */
	public FacetProfileImpl(final LinkedHashMap<String, String> map) {
		
		for (final String key : map.keySet()) {
			facets.put(key, new FacetImpl(key, map.get(key), ""));
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, Facet> getTopLevelFacets() {
		return Collections.unmodifiableMap(facets);
	}

}
