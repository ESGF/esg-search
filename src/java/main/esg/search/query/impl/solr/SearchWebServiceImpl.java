package esg.search.query.impl.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import esg.search.query.api.FacetProfile;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;
import esg.search.query.api.SearchWebService;

/**
 * Implementation of {@link SearchWebService} that delegates all functionality to the underlying {@link SearchService}.
 * 
 * @author luca.cinquini
 *
 */
@Service
public class SearchWebServiceImpl implements SearchWebService {
	
	/**
	 * The underlying search service to which all calls are delegated.
	 */
	final private SearchServiceImpl searchService;
	
	/**
	 * The application specific facet profile, 
	 * i.e. the set of facets that can be returned to decorate the search results.
	 */
	final private FacetProfile facetProfile;
	
	/**
	 * Unmodifiable empty object used to speed up unconstrained calls.
	 */
	final private Map<String,String[]> emptyConstraints = Collections.unmodifiableMap( new HashMap<String, String[]>() );
	
	@Autowired
	public SearchWebServiceImpl(final SearchServiceImpl searchService, final @Qualifier("wsFacetProfile") FacetProfile facetProfile) {
		this.searchService = searchService;
		this.facetProfile = facetProfile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String search(final String text, final Map<String, String[]> constraints, 
			             int offset, int limit, boolean getResults, boolean getFacets, final SearchReturnType returnType) throws Exception {
		
		// build search input object
		final SearchInput input = new SearchInputImpl();
		if (StringUtils.hasLength(text)) input.setText(text);
		for (final String facet : constraints.keySet()) {
			for (final String value : constraints.get(facet)) {
				input.addConstraint(facet, value);
			}
		}
		input.setOffset(offset);
		input.setLimit(limit);
		if (getFacets) input.setFacets(new ArrayList<String>(facetProfile.getTopLevelFacets().keySet()));
		
		// execute HTTP search request
		final String xml = searchService.getXml(input, getResults, getFacets);
		
		// return HTTP search response in requested format
		if (returnType.equals(SearchReturnType.XML)) {
			return xml;
			
		} else {
			// JSON not yet supported
			throw new Exception("Unsupported results format: "+returnType);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchByTimeStamp(String fromTimeStamp, String toTimeStamp, 
			                        int offset, int limit, boolean getResults, boolean getFacets, SearchReturnType returnType) throws Exception {
		
		// express timestamp search as text query (example: "timestamp:[2010-10-19T22:00:00Z TO NOW])"
		final String text = SolrXmlPars.FIELD_TIMESTAMP+":["+fromTimeStamp+" TO "+toTimeStamp+"]";
		
		// execute call
		return this.search(text, emptyConstraints, offset, limit, getResults, getFacets, returnType);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchById(String idMatch, 
			                 int offset, int limit, boolean getResults, boolean getFacets, SearchReturnType returnType) throws Exception {
		
		// express search by id as text query (also in wildcard case) 
		final String text = SolrXmlPars.FIELD_ID+":"+idMatch;
		
		// execute call
		return this.search(text, emptyConstraints, offset, limit, getResults, getFacets, returnType);
		
	}
	
}
