package esg.search.query.ws.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import esg.search.query.ws.hessian.SearchWebService;

/**
 * Web controller that supports RESTful invocations of the metadata search service.
 * This controller delegates all functionality to the underlying {@link SearchWebService}.
 * 
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
	@RequestMapping(value="/rest/search/", method=RequestMethod.GET)
	@SuppressWarnings("unchecked")
	public void search(final HttpServletRequest request, 
			           final @ModelAttribute(COMMAND) SearchRestCommand command, 
			           final HttpServletResponse response) throws Exception {
	    		
		// execute query
		final String xml = searchWebService.search(command.getText(), request.getParameterMap(), 
				                                   command.getOffset(), command.getLimit(), command.isResults(), command.isFacets(), command.getBack());
		writeToResponse(xml, response);
	}
	
	/**
	 * REST method to search documents by matching id.
	 * Allowed HTTP parameters (besides the common parameters listed above):
	 * @param id : expression matching the document(s) id
	 */
	@RequestMapping(value="/rest/searchById/", method=RequestMethod.GET)
	public void searchById(@RequestParam("id") String id, @RequestParam(value="type", required=false) String type, 
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
	@RequestMapping(value="/rest/searchByTimeStamp/", method=RequestMethod.GET)
	public void searchByTimeStamp(@RequestParam("from") String from, @RequestParam("to") String to, @RequestParam(value="type", required=false) String type,
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