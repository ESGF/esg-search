package eske.service.query.wrappers;

import java.util.List;

import esg.search.core.Record;
import esg.search.query.api.SearchOutput;
import esg.search.query.impl.solr.SolrXmlPars;
import eske.model.query.QueryOutput;
import eske.model.query.QueryResult;
import eske.service.query.api.QueryException;

/**
 * Factory to translate the output of a search operation.
 */
public class OutputFactory {
	
	/**
	 * Utility class to translate facet and product keys.
	 */
	private final MappingUtil mappingUtil;
	
	/**
	 * Constructor must be supplied with maps to translate facet and product keys.
	 * @param facetKeyMap
	 */
	public OutputFactory(final MappingUtil mappingUtil) {
		this.mappingUtil = mappingUtil;
	}

	/**
	 * Factory method to translate the full search output.
	 * @param output
	 * @return
	 */
	public QueryOutput getInstance(final SearchOutput output) throws QueryException {
		
		final QueryOutput _output = new QueryOutput();
		
		// number of results
		_output.setCounts(output.getCounts());
		
		// loop over results
		for (final Record record : output.getResults()) {
			_output.addResult( getInstance(record), true); // first=true
		}
		
		return _output;
		
	}
	
	/**
	 * Factory method to translate a single search result.
	 * @param record
	 * @return
	 */
	public QueryResult getInstance(final Record record) throws QueryException {
		
		final QueryResult result = new QueryResult(record.getId());
		for (final String name : record.getFields().keySet()) {
			final List<String> values = record.getFields().get(name);
			if (name.equals(SolrXmlPars.FIELD_TITLE) || name.equals(SolrXmlPars.FIELD_NAME)) {
				result.setName( values.get(0) );
			} else if (name.equals(SolrXmlPars.FIELD_DESCRIPTION)) {
				result.setDescription( values.get(0) );
			} else if (name.equals(SolrXmlPars.FIELD_URL)) {
				result.setUrl( values.get(0) );
			} else if (name.equals(SolrXmlPars.FIELD_TYPE)) {
				final String value = values.get(0);
				result.setType( mappingUtil.getInverseProductMapping(value) );
			} else {
				for (final String value : values) {
					result.getFacets().addValue(name, value, value);
				}
			}
		}
		
		return result;
	}
		
}
