package esg.search.query.ws.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import esg.search.query.api.FacetProfile;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;
import esg.search.query.ws.hessian.SearchWebService;

/**
 * Web controller that supports RESTful invocations of the metadata search service.
 * This controller delegates all functionality to the underlying {@link SearchWebService}.
 * 
 * TODO: review parameters list
 * All REST methods accept the following optional HTTP parameters (in addition to the ones specifically listed in each method),
 * which all have sensible default values.
 * 
 * <ul>
 * 	<li>@param offset : number of skipped results before current list (default: 0)
 * 	<li>@param limit : maximum number of returned results (default: 10)
 * 	<li>@param results : true to include document results in the HTTP response (default: true)
 * 	<li>@param facets : true to include facet results in the HTTP response (default: true)
 * 	<li>@param back : the type of the returned HTTP response (default: XML)
 * </ul>
 * 
 * @author luca.cinquini
 *
 */
@Controller("searchRestController")
public class SearchRestController {
	
    /**
     * The underlying search service to which all calls are delegated.
     */
    final private SearchService searchService;
    
    /**
     * The application specific facet profile, 
     * i.e. the set of facets that can be returned to decorate the search results.
     */
    final private FacetProfile facetProfile;
	
	private final static String COMMAND = "search_command";
	
	private final Log LOG = LogFactory.getLog(this.getClass());
		
	@Autowired
	public SearchRestController(final SearchService searchService, final @Qualifier("wsFacetProfile") FacetProfile facetProfile) {
	      this.searchService = searchService;
	      this.facetProfile = facetProfile;
	}
	
	/**
	 * REST method to execute the most generic possible query.
	 * Allowed HTTP parameters (besides the common parameters listed above):
	 * @param text : search text (example: "?text=...")
	 * @param facet : name of one of the facets in the application facet profile (example: "experiment=control")
	 */
	@RequestMapping(value="/ws/rest/search/", method={ RequestMethod.GET, RequestMethod.POST })
	//@SuppressWarnings("unchecked")
	public void search(final HttpServletRequest request, 
			           final @ModelAttribute(COMMAND) SearchRestCommand command, 
			           final HttpServletResponse response) throws Exception {
	    
	    // set of allowed facets (and fields)
        final Set<String> allowedFacets = facetProfile.getTopLevelFacets().keySet();
	    
	    // check all HTTP parameters for bad characters
	    for (final Object obj : request.getParameterMap().keySet()) {
	        
	        // check parameter name
	        String key = obj.toString();
	        final Matcher keyMatcher = QueryParameters.INVALID_CHARACTERS.matcher(key);
            if (keyMatcher.matches()) sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                                "Invalid character(s) detected in parameter name="+key,
                                                response);                                                        
            
            // check parameter values
            String[] values = request.getParameterValues(key);
            for (int i=0; i<values.length; i++) {
                final Matcher valueMatcher = QueryParameters.INVALID_CHARACTERS.matcher(values[i]);
                if (!StringUtils.hasText(values[i])) sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                                               "Invalid empty value for parameter="+key,
                                                               response);
                if (valueMatcher.matches()) sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                                      "Invalid character(s) detected in parameter value="+values[i],
                                                      response); 
                                                                                  
            }
	        
	    }
        
	    // loop over HTTP parameters, bind to SearchInput fields
	    // Note: the following parameters are automatically bound by Spring:
	    // &type=...&offset=...&limit=...
        for (final Object obj : request.getParameterMap().keySet()) {
            final String parName = (String)obj;
                            
            // &id=...
            if (parName.equals(QueryParameters.ID)) {
                command.addConstraint(parName, request.getParameter(parName) );
             
            // &from=...&to=...
            } else if (parName.equals(QueryParameters.FROM) || parName.equals(QueryParameters.TO)) {    
                // example: 2010-10-19T22:00:00Z TO NOW
                command.addConstraint(parName, request.getParameter(parName));
                         
            // interpret all non-keyword constraints as facets
            // check versus the configured facet profile to allow no unknown facets
            // &facet1=value1&facet2=value2
            } else if (!QueryParameters.KEYWORDS.contains( parName.toLowerCase() )) {
                if (!facetProfile.getTopLevelFacets().containsKey(parName)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported facet "+parName);
                }	             
                final String[] parValues = request.getParameterValues(parName);
                for (final String parValue : parValues) command.addConstraint(parName, parValue);
            }
        }
        
        // &format=
        SearchReturnType format = SearchReturnType.forMimeType(command.getFormat());
        if (format==null) sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, 
                                    "Invalid requested format: "+ command.getFormat(), response);
        
        // &facets=facet1,facet2,...
        // must process comma-separated list from HTTP request into list of string values
        if (!command.getFacets().isEmpty()) {
            for (String facets : command.getFacets()) {
                // special value: include all configured facets
                if (facets.equals("*")) {
                    command.setFacets(new ArrayList<String>(allowedFacets));
                } else {
                    command.setFacets( Arrays.asList( facets.split("\\s*,\\s*") ));
                    // check facet keys are contained in controlled vocabulary
                    for (String facet : command.getFacets()) {
                        if (!allowedFacets.contains(facet)) {
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported facet "+facet);
                        }
                    }
                }
            }            
        }
        
        // &fields=field1,field2,...
        // must process comma-separated list from HTTP request into list of string values
        if (!command.getFields().isEmpty()) {
            // initialize set of returned fields to standard metadata fields
            Set<String> fields = new HashSet<String>(QueryParameters.STANDARD_FIELDS);
            for (String values : command.getFields()) {
                // special value: include all fields
                if (values.equals("*")) {
                    fields = (new HashSet<String>( Arrays.asList(new String[]{"*"})));
                    break;
                } else {
                    String[] _values = values.split("\\s*,\\s*");
                    for (String value : _values) {
                        if (!fields.contains(value)) {
                            if (allowedFacets.contains(value)) {
                                fields.add(value);
                            } else {
                                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported field "+value);
                            }
                        }
                    }
                }
            }
            command.setFields(fields);
        }

        // execute HTTP search request, return response
        if (!response.isCommitted()) {
        	                 
            String output = searchService.query(command, format); // isfacets=true FIXME
            writeToResponse(output, response);
                        
        }
	    	    		
	}
		
	private void writeToResponse(final String content, final HttpServletResponse response) throws Exception {
		response.setContentType("text/xml");
		response.getWriter().write( content );
	}

	private void sendError(int sc, final String message, final HttpServletResponse response) throws IOException {
        LOG.warn(message);
        response.sendError(sc, message);
	}
}