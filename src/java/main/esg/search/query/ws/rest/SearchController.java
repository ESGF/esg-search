package esg.search.query.ws.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import esg.search.query.api.SearchReturnType;
import esg.search.query.api.SearchService;

/**
 * Web controller that supports RESTful invocations of the metadata search service.
 * This controller delegates all functionality to the underlying {@link SearchService}.
 * The HTTP request parameters are specified by the ESGF Search API.
 * 
 * @author luca.cinquini
 *
 */
@Controller("searchController")
public class SearchController {
    
    private static final Log LOG = LogFactory.getLog(SearchController.class);
	
    /**
     * The underlying base controller to which all calls are delegated.
     */
    final private BaseController baseController;
    
		
	@Autowired
	public SearchController(final BaseController baseController) {
	      this.baseController = baseController;
	}
	
	/**
	 * Method to execute a generic metadata search and return the untransformed Solr/XML output document.
	 */
	@RequestMapping(value="/search", method={ RequestMethod.GET, RequestMethod.POST })
	public void search(final HttpServletRequest request, 
			           final SearchCommand command, 
			           final HttpServletResponse response) throws Exception {
	    
	    long startTime = System.currentTimeMillis();
	    
	    // process request, obtain Solr/XML output
        String output = baseController.process(request, command, response);
        
        // write Solr/XML to response
        if (!response.isCommitted()) {
            if (command.getFormat().equals(SearchReturnType.SOLR_JSON.getMimeType())) {
                baseController.writeToResponse(output, "text/json", response); 
            } else {
                baseController.writeToResponse(output, "text/xml", response); 
            }
        }
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (LOG.isInfoEnabled()) LOG.info("Overall SearchController Elapsed Time="+elapsedTime+" msecs");
	    
	}
	
}