package esg.search.query.ws.hessian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import esg.search.query.api.FacetProfile;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;
import esg.search.query.impl.solr.SearchServiceImpl;
import esg.search.query.impl.solr.SolrXmlPars;

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
	
	private static final Log LOG = LogFactory.getLog(SearchWebServiceImpl.class);
	
	/**
	 * List of invalid text characters - anything that is not within square brackets.
	 */
	//private static Pattern pattern = Pattern.compile(".*[^a-zA-Z0-9_\\-\\.\\@\\'\\:\\;\\,\\s/()].*");

	
	@Autowired
	public SearchWebServiceImpl(final SearchServiceImpl searchService, final @Qualifier("wsFacetProfile") FacetProfile facetProfile) {
		this.searchService = searchService;
		this.facetProfile = facetProfile;
	}

	/**
	 * {@inheritDoc}
	 * Note that this method implementation "sanitizes" the constraints map by just accepting keys that are explicitely
	 * defined in the application facet profile, and by checking the constraint values for inalid characters.
	 */
	@Override
	public String search(final String text, final Map<String, String[]> constraints, 
			             int offset, int limit, boolean getResults, boolean getFacets, final SearchReturnType returnType) throws Exception {
		
		// build search input object
		final SearchInput input = new SearchInputImpl();
		if (StringUtils.hasLength(text)) input.setText(text);
		
		// parse constraints
		// for security reasons, loop ONLY over parameter keys found in facet profile, matching against HTTP request parameters,
		// and check constraint values for invalid characters
		for (final String parName : facetProfile.getTopLevelFacets().keySet()) {
			final String[] parValues = constraints.get(parName);
			if (parValues!=null) {
				for (final String parValue : parValues) {
					if (StringUtils.hasText(parValue)) {
						input.addConstraint(parName, parValue);
						if (LOG.isTraceEnabled()) LOG.trace("Set constraint name="+parName+" value="+parValue);
					}
				}
			}
			
		}

		input.setOffset(offset);
		input.setLimit(limit);
		if (getFacets) input.setFacets(new ArrayList<String>(facetProfile.getTopLevelFacets().keySet()));
		
		// execute HTTP search request, return response
		return searchService.query(input, getResults, getFacets, returnType);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchByTimeStamp(final String fromTimeStamp, final String toTimeStamp, 
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