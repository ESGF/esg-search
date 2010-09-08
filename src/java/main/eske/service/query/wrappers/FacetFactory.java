package eske.service.query.wrappers;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Factory class to convert a {@link esg.search.query.Facet} instance into a {@link eske.model.query.Facet} instance. 
 */
public class FacetFactory {
	
	/**
	 * Utility class to translate facet and product keys.
	 */
	private final MappingUtil mappingUtil;
	
	public FacetFactory(final MappingUtil mappingUtil) {
		
		this.mappingUtil = mappingUtil;	

	}
	
	/**
	 * Factory method that transforms one hierarchy of facet objects into another.
	 * @param facetKey : the key to assign to the newly created eske.model.query.Facet
	 * @param facet : the input esg.search.api.Facet
	 * @return : the output eske.model.query.Facet
	 */
	public eske.model.query.Facet getFacetInstance(esg.search.query.api.Facet facet) {
		
		final String _facetKey = mappingUtil.getInverseFacetMapping(facet.getKey());
		final eske.model.query.Facet _facet = new eske.model.query.Facet(_facetKey, facet.getLabel(), facet.getDescription());
		for (final esg.search.query.api.Facet subFacet : facet.getSubFacets()) {
			final eske.model.query.Facet _subFacet = new eske.model.query.Facet(subFacet.getKey(), subFacet.getLabel(), subFacet.getDescription());
			_facet.addSubFacet(_subFacet);
		}
		return _facet;

	}
	
	/**
	 * Utility method that translates a map of {@link esg.search.query.Facet} objects into a map of string options.
	 * @param facets
	 * @return
	 */
	public Map<String, Set<String>> getFacetMap(final Map<String, esg.search.query.api.Facet> facets) {
		
		final Map<String, Set<String>> _facets = new HashMap<String, Set<String>>();
		
		for (final String facetKey : facets.keySet()) {

			final String _facetKey = mappingUtil.getInverseFacetMapping(facetKey);
			final Set<String> options = new LinkedHashSet<String>();
			for (final esg.search.query.api.Facet subFacet : facets.get(facetKey).getSubFacets()) {
				options.add(subFacet.getKey());
			}
			_facets.put(_facetKey, options);
			
		}
		
		return _facets;
	}

}
