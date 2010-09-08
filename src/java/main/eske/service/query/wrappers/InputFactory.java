package eske.service.query.wrappers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.util.StringUtils;

import esg.search.query.api.SearchInput;
import esg.search.query.impl.solr.SearchInputImpl;
import eske.model.query.QueryInput;
import eske.service.query.api.QueryException;

/**
 * Factory class to convert a {@link SearchInput} instance into a {@link QueryInput} instance.
 */
public class InputFactory {
	
	// ESG parameters
	private final static String TYPE = "type";
	
	// ESKE parameters 
	private final static String TEXT = "hasText";
	
	/**
	 * Utility class to translate facet and product keys.
	 */
	private final MappingUtil mappingUtil;
	
	/**
	 * Constructor must be supplied with maps to translate facet and product keys.
	 * @param facetKeyMap
	 */
	public InputFactory(final MappingUtil mappingUtil) {
		this.mappingUtil = mappingUtil;
	}
	
	/**
	 * Main factory method.
	 * @param input
	 * @param facetKeys
	 * @return
	 */
	public SearchInput getInstance(final QueryInput input, final Collection<String> facetKeys) throws QueryException {
		
		final SearchInput _input = new SearchInputImpl();
		
		// text, constraints
		for (final String name : input.getConstraints().keySet()) {
			final List<String> values = input.getConstraints().get(name);
			if (name.equals(TEXT)) {
				_input.setText(values.get(0));
			} else {
				_input.addConstraints(mappingUtil.getFacetMapping(name), values);
			}
		}
		
		// product
		final String product = input.getProduct();
		if (StringUtils.hasText(product)) _input.setType( mappingUtil.getProductMapping(product) );
		//_input.addConstraint(TYPE, mappingUtil.getProductMapping(product));
				
		// facets
		for (final String facetKey : facetKeys) {
			_input.addFacet(mappingUtil.getFacetMapping(facetKey));
		}

		// offset, limit
		_input.setOffset(input.getOffset());
		_input.setLimit(input.getLimit());

		return _input;
		
	}
	
	/**
	 * Shortcut method where no output facets are specified.
	 * @param input
	 * @param facetKeys
	 * @return
	 * @throws QueryException
	 */
	public SearchInput getInstance(final QueryInput input) throws QueryException {
		return this.getInstance(input, new HashSet<String>());
	}

}
