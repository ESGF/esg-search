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

import esg.search.query.api.FacetProfile;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;

/**
 * Class containing the base functionality for executing RESTful requests to the ESGF metadata services.
 * It contains methods to validate the incoming HTTP request, invoke the functionality of the underlying {@link SearchService},
 * and write the search results to the HTTP response. 
 * These methods are invoked by the controllers that expose the actual RESTful search endpoints.
 * 
 * The HTTP request parameters are specified by the ESGF Search API.
 * 
 * In case of error reported by the underlying {@link SearchService}, the controller attempts to recover by modifying the query
 * endpoints of the distributed search, and attempts the query again an additional numberOfTries times. 
 * 
 * @author luca.cinquini
 *
 */
@Controller("baseController")
public class BaseController {
	
    /**
     * The underlying search service to which all calls are delegated.
     */
    final private SearchService searchService;
    
    /**
     * The application specific facet profile, 
     * i.e. the set of facets that can be returned to decorate the search results.
     */
    final private FacetProfile facetProfile;
    
    /**
     * Number of query attempts:
     * 1 - with current shards list
     * 2 - with pruned shards list
     * 3 - with local shard only
     */
    private int numberOfTries = 3;
		
	private final Log LOG = LogFactory.getLog(this.getClass());
		
	@Autowired
	public BaseController(final SearchService searchService, final @Qualifier("wsFacetProfile") FacetProfile facetProfile) {
	      this.searchService = searchService;
	      this.facetProfile = facetProfile;
	}
	
	/**
	 * Method that processes the incoming HTTP request, invokes the back-end search service,
	 * and returns the output document in Solr/XML format.
	 * This method can be invoked by other controllers wishing to post-process the output document.
	 * 
	 * @param request
	 * @param command
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String process(final HttpServletRequest request, 
            final SearchCommand command, 
            final HttpServletResponse response) throws Exception {
	    	    
	    // set of allowed facets (and fields)
        final Set<String> allowedFacets = facetProfile.getTopLevelFacets().keySet();
        
        // impose maximum count on returned results
        if (command.getLimit()>QueryParameters.MAX_LIMIT) {
            sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Too many records requested, maximum allowed value is limit="+QueryParameters.MAX_LIMIT,
                    response);  
        }
	    
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
            // NOTE: only one value allowed
            if (parName.equals(QueryParameters.ID)) {
                command.addConstraint(parName, request.getParameter(parName) );
                
            // &replica=true|false (or True|False or T|F)
            // NOTE: only one value allowed
            } else if (   parName.equalsIgnoreCase(QueryParameters.REPLICA)) {
                command.addConstraint(parName, request.getParameter(parName));
             
            // other keywords
            // &from=...&to=... &start=... &end=... &bbox=
            // these constraints are interpreted specially by the SearchService implementation
            } else if (   parName.equals(QueryParameters.FROM) || parName.equals(QueryParameters.TO)
                       || parName.equals(QueryParameters.START) || parName.equals(QueryParameters.END)
                       || parName.equals(QueryParameters.BBOX) ) {
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
                                sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported field "+value, response);
                            }
                        }
                    }
                }
            }
            command.setFields(fields);
        }

        
        if (!response.isCommitted()) {
        
            // attempt query numberOfTries times
            for (int n=0; n<numberOfTries; n++) {    
                try {
                    // invoke back-end search service (HTTP request to Solr), return response document
                    return searchService.query(command, format);
                } catch(Exception e) {
                    LOG.warn(e.getMessage());
                    if (n<numberOfTries-1) {
                        if (LOG.isDebugEnabled()) LOG.debug("Query failed "+n+" times, attempting to recover from search error");
                        // attempt to recover from error
                        recover(command, n);
                    } else {
                        // send error to client
                        throw e;
                    }
                }
            }
            
        }
        
        // response error, return empty body content
        return "";
	    	    		
	}
	
	/**
	 * Method to write a string content to the HTTP response object,
	 * @param content
	 * @param contentType
	 * @param response
	 * @throws Exception
	 */
	void writeToResponse(final String content, final String contentType, final HttpServletResponse response) throws Exception {
		response.setContentType(contentType);
		response.getWriter().write( content );
	}

	/**
	 * Method to return an HTTP error in the response.
	 *  
	 * @param sc
	 * @param message
	 * @param response
	 * @throws IOException
	 */
	void sendError(int sc, final String message, final HttpServletResponse response) throws IOException {
        LOG.warn(message);
        response.sendError(sc, message);
	}
	
	/**
	 * Method to modify the query parameters after a search error, 
	 * in to attempt a new search.
	 * 
	 * @param input : the current search input parameters
	 * @param n : the current attempt number (0 after the first failure, and so son...)
	 */
	void recover(final SearchInput input, int n) {
	   
	    if (n==0) {
	        // ask the node manager to prune the shards list
	        if (LOG.isDebugEnabled()) LOG.debug("Pruning the shards list");
	        prune();
	        
	    } else {
	        // execute non-distributed query
	        if (LOG.isDebugEnabled()) LOG.debug("Executing a non-distributed query");
	        input.setDistrib(false);
	    }
	    
	}
	
	// stub
	boolean prune() {
	    return true;
	}

	/**
	 * Setter method for number of additional query attempts.
	 * @param numberOfTries
	 */
    public void setNumberOfTries(int numberOfTries) {
        this.numberOfTries = numberOfTries;
    }
	
}