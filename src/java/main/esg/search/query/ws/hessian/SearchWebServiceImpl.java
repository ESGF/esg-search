package esg.search.query.ws.hessian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import esg.search.query.api.FacetProfile;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;
import esg.search.query.impl.solr.SearchServiceImpl;

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
	//final private Map<String,String[]> emptyConstraints = Collections.unmodifiableMap( new HashMap<String, String[]>() );
	
	private static final Log LOG = LogFactory.getLog(SearchWebServiceImpl.class);
		
	@Autowired
	public SearchWebServiceImpl(final SearchServiceImpl searchService, final @Qualifier("wsFacetProfile") FacetProfile facetProfile) {
		this.searchService = searchService;
		this.facetProfile = facetProfile;
	}

	/**
	 * {@inheritDoc}
	 * Note that this method implementation "sanitizes" the constraints map by just accepting keys that are explicitly
	 * defined in the application facet profile, and by checking the constraint values for invalid characters.
	 */
	@Override
	public String search(final String query, final String type, final Map<String, String[]> constraints,
			             int offset, int limit, boolean distrib,
			             final SearchReturnType returnType) throws Exception {
		
	    if (LOG.isInfoEnabled()) {
	        LOG.info("query="+query);
	        LOG.info("offset="+offset+" limit="+limit+" distrib="+distrib);
	    }
		// build search input object
		final SearchInput input = new SearchInputImpl();
		if (StringUtils.hasLength(query)) input.setQuery(query);
		
		// parse constraints
		// for security reasons, loop ONLY over parameter keys found in facet profile, matching against HTTP request parameters,
		// and check constraint values for invalid characters
		// allow additional parameter "dataset_id" to enable generic queries of files

		// result type
		if (StringUtils.hasText(type)) {
		    input.setType(type);
		}
		
		// interpret all non-keyword constraints as facets
		// check versus the configured facet profile
		for (final String parName : constraints.keySet()) {
		    if (!QueryParameters.KEYWORDS.contains( parName.toLowerCase() )) {
    			final String[] parValues = constraints.get(parName);
    			if (parValues!=null) {
    				for (final String parValue : parValues) {
    					if (StringUtils.hasText(parValue)) {
    					    final Matcher matcher = QueryParameters.INVALID_CHARACTERS.matcher(parValue);
    					    if (matcher.matches()) throw new Exception("Invalid character(s) detected in parameter value="+parValue);
    						input.addConstraint(parName, parValue);
    						if (LOG.isTraceEnabled()) LOG.trace("Set constraint name="+parName+" value="+parValue);
    					}
    				}
    			}
		    }
		}

		input.setOffset(offset);
		input.setLimit(limit);
		input.setDistrib(distrib);
		input.setFacets(new ArrayList<String>(facetProfile.getTopLevelFacets().keySet()));
		
		// execute HTTP search request, return response
		return searchService.query(input, returnType);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchByTimeStamp(final String fromTimeStamp, final String toTimeStamp, final String type,
			                        int offset, int limit, final SearchReturnType returnType) throws Exception {
		
	    final Map<String,String[]> constraints = new HashMap<String,String[]>();
	    
		// express timestamp search as text query (example: "timestamp:[2010-10-19T22:00:00Z TO NOW])"
		//String text = SolrXmlPars.FIELD_TIMESTAMP+":["+fromTimeStamp+" TO "+toTimeStamp+"]";
	    
	    // mandatory temporal constraints
	    constraints.put(QueryParameters.FROM, new String[]{ fromTimeStamp } );
	    constraints.put(QueryParameters.TO, new String[]{ toTimeStamp } );
		
		// optional type constraint
	    //if (StringUtils.hasText(type)) {
	    //    constraints.put(QueryParameters.TYPE, new String[]{ type } );
	        // text +=  " AND "+SolrXmlPars.FIELD_TYPE+":"+type;
	    //}
		
		// execute call
		return this.search(null, type, constraints, offset, limit, true, returnType);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchById(String idMatch, final String type,
			                 int offset, int limit, final SearchReturnType returnType) throws Exception {
		
	    final Map<String,String[]> constraints = new HashMap<String,String[]>();
	    
	    // mandatory id constraint
	    constraints.put(QueryParameters.ID, new String[]{ idMatch } );
				
		// execute call
		return this.search(null, type, constraints, offset, limit, true, returnType);
		
	}
	
}
