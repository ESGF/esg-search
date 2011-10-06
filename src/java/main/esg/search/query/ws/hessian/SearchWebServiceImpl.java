package esg.search.query.ws.hessian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	/**
	 * List of invalid text characters - anything that is not within square brackets.
	 */
	private static Pattern pattern = Pattern.compile(".*[^a-zA-Z0-9_\\-\\.\\@\\'\\:\\;\\,\\s/()\\*].*");
	
	/**
	 * List of HTTP parameter names that are NOT interpreted as facet constraints.
	 */
	private final static List<String> RESERVED_PARAMATER_NAMES = Arrays.asList(new String[] { "offset", "limit", "facets", "results", "text", "back" });

	
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
			             boolean getResults, boolean getFacets, final SearchReturnType returnType) throws Exception {
		
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
		
		for (final String parName : constraints.keySet()) {
		    if (!RESERVED_PARAMATER_NAMES.contains( parName.toLowerCase() )) {
    			final String[] parValues = constraints.get(parName);
    			if (parValues!=null) {
    				for (final String parValue : parValues) {
    					if (StringUtils.hasText(parValue)) {
    					    final Matcher matcher = pattern.matcher(parValue);
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
		if (getFacets) input.setFacets(new ArrayList<String>(facetProfile.getTopLevelFacets().keySet()));
		
		// execute HTTP search request, return response
		return searchService.query(input, getResults, getFacets, returnType);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchByTimeStamp(final String fromTimeStamp, final String toTimeStamp, final String type,
			                        int offset, int limit, boolean getResults, boolean getFacets, SearchReturnType returnType) throws Exception {
		
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
		return this.search(null, type, constraints, offset, limit, true, getResults, getFacets, returnType);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String searchById(String idMatch, final String type,
			                 int offset, int limit, boolean getResults, boolean getFacets, SearchReturnType returnType) throws Exception {
		
	    final Map<String,String[]> constraints = new HashMap<String,String[]>();
	    
	    // mandatory id constraint
	    constraints.put(QueryParameters.ID, new String[]{ idMatch } );
				
		// execute call
		return this.search(null, type, constraints, offset, limit, true, getResults, getFacets, returnType);
		
	}
	
}
