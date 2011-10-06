package esg.search.query.ws.rest;

import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import esg.search.query.api.QueryParameters;
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
	 * The underlying search web service to which all calls are delegated.
	 */
	final SearchWebService searchWebService;
	
	private final static String COMMAND = "search_command";
		
	@Autowired
	public SearchRestController(final SearchWebService searchWebService) {
		this.searchWebService = searchWebService;
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
	    
	    // check all parameters for bad characters
	    String error = "";
	    for (final Object obj : request.getParameterMap().keySet()) {
	        
	        // check parameter name
	        String key = obj.toString();
	        final Matcher keyMatcher = QueryParameters.INVALID_CHARACTERS.matcher(key);
            if (keyMatcher.matches()) error ="Invalid character(s) detected in parameter name="+key;
            
            // check parameter values
            String[] values = request.getParameterValues(key);
            for (int i=0; i<values.length; i++) {
                final Matcher valueMatcher = QueryParameters.INVALID_CHARACTERS.matcher(values[i]);
                if (valueMatcher.matches()) error ="Invalid character(s) detected in parameter value="+values[i];
            }
	        
	    }
	    
	    // bad characters --> 404 HTTP response
	    if (StringUtils.hasText(error)) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
	    
	    } else {
	        
	        // execute query
	        final String xml = searchWebService.search(command.getQuery(), command.getType(), request.getParameterMap(),
	                                                   command.getOffset(), command.getLimit(), command.isDistrib(), command.isResults(), command.isFacets(), command.getBack());
	        writeToResponse(xml, response);
	    }
	    	    		
	}
	
	/**
	 * REST method to search documents by matching id.
	 * Allowed HTTP parameters (besides the common parameters listed above):
	 * @param id : expression matching the document(s) id
	 */
	@RequestMapping(value="/ws/rest/searchById/", method=RequestMethod.GET)
	public void searchById(@RequestParam(QueryParameters.ID) String id, @RequestParam(value=QueryParameters.TYPE, required=false) String type, 
			               final @ModelAttribute(COMMAND) SearchRestCommand command,
			               final HttpServletResponse response) throws Exception {
		
		final String xml = searchWebService.searchById(id, type,
				                                       command.getOffset(), command.getLimit(), command.isResults(), command.isFacets(), command.getBack());
		writeToResponse(xml, response);
	}
	
	/**
	 * REST method to search documents by their last updated time stamp.
	 * Allowed HTTP parameters (besides the common parameters listed above):
	 * @param from : lower limit for last update date and time in ISO8601 format (example: "2010-10-19T22:00:00Z") 
	 * @param to : upper limit for last update date and time in ISO8601 format (example: "2010-10-19T22:00:00Z") , 
	 *             or special strings (example: "NOW")
	 */
	@RequestMapping(value="/ws/rest/searchByTimeStamp/", method=RequestMethod.GET)
	public void searchByTimeStamp(@RequestParam(QueryParameters.FROM) String from, 
	                              @RequestParam(QueryParameters.TO) String to, 
	                              @RequestParam(value=QueryParameters.TYPE, required=false) String type,
			                      final @ModelAttribute(COMMAND) SearchRestCommand command,
			                      final HttpServletResponse response) throws Exception {
		
		final String xml = searchWebService.searchByTimeStamp(from, to, type,
                                                              command.getOffset(), command.getLimit(), command.isResults(), command.isFacets(), command.getBack());
		writeToResponse(xml, response);
	}
	
	
	private void writeToResponse(final String content, final HttpServletResponse response) throws Exception {
		response.setContentType("text/xml");
		response.getWriter().write( content );
	}

}