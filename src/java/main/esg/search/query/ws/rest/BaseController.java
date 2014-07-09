package esg.search.query.ws.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
	    	    	    
	    // check all HTTP parameters:
	    //  -) reject if they contain bad characters
	    //  -) reject if not contained in list of allowed parameters
	    for (final Object obj : request.getParameterMap().keySet()) {
	        
	        // check parameter name
	        String key = obj.toString();
	        final Matcher keyMatcher = QueryParameters.INVALID_CHARACTERS.matcher(key);
            if (keyMatcher.matches())
                    return sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                     "Invalid character(s) detected in parameter name="+key,
                                     response);  
            
            // check parameter values
            String[] values = request.getParameterValues(key);
            for (int i=0; i<values.length; i++) {
                final Matcher valueMatcher = QueryParameters.INVALID_CHARACTERS.matcher(values[i]);
                if (!StringUtils.hasText(values[i])) return sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                                               "Invalid empty value for parameter="+key,
                                                               response);
                if (valueMatcher.matches()) return sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                                      "Invalid character(s) detected in parameter value="+values[i],
                                                      response); 
                                                                                  
            }
            
            // check parameter name versus allowed list
            // remove possible trailing '!' for negative constraints
            final String _key = key.replaceAll("!$", "");
            if (   !QueryParameters.KEYWORDS.contains(_key)
                && !QueryParameters.CORE_QUERY_FIELDS.contains(_key)
                && !facetProfile.getTopLevelFacets().keySet().contains(_key)) {
                return sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid HTTP query parameter="+key, response); 
            }
	        
	    }
	            
        // keyword "limit": impose maximum count on returned results
        if (command.getLimit()>QueryParameters.MAX_LIMIT) {
            return sendError(HttpServletResponse.SC_BAD_REQUEST, 
                            "Too many records requested, maximum allowed value is limit="+QueryParameters.MAX_LIMIT,
                             response);  
        }
        
        // keyword "format": check requested output format
        SearchReturnType format = SearchReturnType.forMimeType(command.getFormat());
        if (format==null) return sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, 
                                    "Invalid requested format: "+ command.getFormat(), response);

	    
        // keyword "facets": &facets=facet1,facet2,...
        // -) translate "*" into explicit list of facets defined in facet profile
        // -) process comma-separated list from HTTP request into list of string values
        final Set<String> defaultFacets = facetProfile.getTopLevelFacets().keySet();
        if (!command.getFacets().isEmpty()) {
            for (String facets : command.getFacets()) {
                // special value: include all configured facets
                if (facets.equals("*")) {
                    command.setFacets(new ArrayList<String>(defaultFacets));
                } else {
                    command.setFacets( Arrays.asList( facets.split("\\s*,\\s*") ));
                }
            }            
        }
        
        // keyword "fields": &fields=field1,field2,...
        // -) translate "*" into explicit list of standard fields
        // -) process comma-separated list from HTTP request into list of string values
        if (!command.getFields().isEmpty()) {
            // initialize set of returned fields to standard metadata fields
            //Set<String> fields = new HashSet<String>(QueryParameters.STANDARD_FIELDS);
            for (String fields : command.getFields()) {
                // special value: include all fields
                if (fields.equals("*")) {
                    command.setFields(new HashSet<String>( Arrays.asList(new String[]{"*"})));
                } else {
                    command.setFields( new HashSet<String>( Arrays.asList( fields.split("\\s*,\\s*") ) ) );
                }
            }
        }
        
        // keyword "shards": &shards=shard1, shard2, ...
        // -) process comma-separated list from HTTP request into list of requested shards
        if (!command.getShards().isEmpty()) {
            for (String shards : command.getShards() ) {
                command.setShards( new LinkedHashSet<String>( Arrays.asList( shards.split("\\s*,\\s*") ) ) );
            }
        }
        
	    // loop over HTTP parameters, bind to SearchInput fields
	    // note that keywords are automatically bound by Spring to the SearchInput fields
	    // &query=... &offset=... &limit=... &format=... &facets=... &fields=... &distrib=... &shards=... &from=... &to=...
        for (final Object obj : request.getParameterMap().keySet()) {
            final String parName = (String)obj;
            if (!QueryParameters.KEYWORDS.contains( parName.toLowerCase() )) {
             
                // Unsupported fields
                if (   parName.equals(QueryParameters.FIELD_LAT) 
                    || parName.equals(QueryParameters.FIELD_LON)
                    || parName.equals(QueryParameters.FIELD_LOCATION) 
                    || parName.equals(QueryParameters.FIELD_RADIUS)
                    || parName.equals(QueryParameters.FIELD_POLYGON) ) {
                    
                    return sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported parameter: "+parName, response);
                          
                // SINGLE-VALUED CONSTRAINTS (only parse first HTTP parameter value)
                // &type=...
                // &replica=true|false (or True|False or T|F)
                // NOTE: only one value allowed
                } else if (   parName.equals(QueryParameters.FIELD_TYPE)
                           || parName.equals(QueryParameters.FIELD_REPLICA)
                           || parName.equals(QueryParameters.FIELD_START)
                           || parName.equals(QueryParameters.FIELD_END)
                           || parName.equals(QueryParameters.FIELD_BBOX)) {
                    command.setConstraint(parName, request.getParameter(parName) );
              
              
                // MULTI-VALUED CONSTRAINTS (parse all HTTP parameter values)
                // &facet1=value1&facet1=value2
                // OR:
                // &facets1=value1,value2,...
                } else {
                    final String[] parValues = request.getParameterValues(parName);
                    for (final String parValue : parValues) {
                    	String[] _parValues = parValue.split("\\s*,\\s*");
                    	for (String _parValue : _parValues) {
                    		command.addConstraint(parName, _parValue);
                    	}
                    }
                }
                
            }
        }
        
        // Default limit of 1000 for file queries (for generation of wget scripts)
        //if (command.getConstraint(QueryParameters.FIELD_TYPE).equals(QueryParameters.TYPE_FILE)
        //    && request.getParameter(QueryParameters.LIMIT)==null) {
        //    command.setLimit(QueryParameters.DEFAULT_LIMIT);
        //}
        
        if (!response.isCommitted()) {
            
            // invoke back-end search service (HTTP request to Solr), return response document
            return searchService.query(command, format);
            
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
	 * The response body itself is blank.
	 *  
	 * @param sc
	 * @param message
	 * @param response
	 * @throws IOException
	 */
	String sendError(int sc, final String message, final HttpServletResponse response) throws IOException {
        LOG.warn(message);
        response.sendError(sc, message);
        return "";
	}
	
}