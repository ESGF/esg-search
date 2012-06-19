package esg.search.query.ws.rest;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SolrXmlOutputDocumentParser;
import esg.search.utils.HttpClient;

/**
 * Controller that returns a wget script for files retrieval.
 * 
 * The files are identified as follows:
 * 1) first a search for datasets matching the supplied search criteria is executed
 * 2) then, a second search is made for files belonging to the matching datasets
 * 
 * The first search may or may not be distributed. 
 * The second search is always a non-distributed search to the index node where the matching datasets were published.
 * 
 * @author Luca Cinquini
 *
 */
@Controller("wgetController2")
public class WgetController2 {
    
    private static final String SCRIPT_NAME = "wget-%s.sh";
    private static final DateFormat timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
    
    public final static int CONNECTION_TIMEOUT = 10000;
    public final static int READ_TIMEOUT = 50000;
    
    // search API endpoint
    //public final static String SEARCH_URI = ":8080/esg-search/search";
    public final static String SEARCH_URI = "/esg-search/search";
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    /**
     * The underlying base controller to which the datasets search is delegated (so that all parameters are validated).
     */
    final private BaseController baseController;
    
    /**
     * Helper object that parses the HTTP search response for datasets, files.
     */
    final private OutputDocumentParser parser = new SolrXmlOutputDocumentParser();
        
    @Autowired
    public WgetController2(final BaseController baseController, final SearchService searchService) {
          this.baseController = baseController;
    }
    
    /**
     * Method to process a search for files matching the given criteria,
     * and return a wget script.
     */
    @RequestMapping(value="/wget2", method={ RequestMethod.GET, RequestMethod.POST })
    public void wget(final HttpServletRequest request, 
                       final SearchCommand command, 
                       final HttpServletResponse response) throws Exception {
        
        // check type=... is not specified
        if (request.getParameter(QueryParameters.FIELD_TYPE)!=null) {
            baseController.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                     "HTTP parameter "+QueryParameters.FIELD_TYPE+" is not allowed in wget requests", 
                                     response);
            return;
        }
    	
        // initialize the wget script object
        WgetScriptGenerator.WgetDescriptor desc = this.getWgetDescriptor(request);
 
        // 1) query for datasets (to specific shards, or fully distributed search, or local search)
        command.setConstraint(QueryParameters.FIELD_TYPE, QueryParameters.TYPE_DATASET);
        // set limit=MAX_LIMIT to enable massive retrieval through wget scripting, unless explicitly set already
        if (request.getParameter(QueryParameters.LIMIT)==null) command.setLimit(QueryParameters.DEFAULT_LIMIT);        
        // process request, obtain Solr/XML output
        String xml = baseController.process(request, command, response);        
                
        // extract datasets from HTTP response, group by index_node
        Map<String, List<String>> datasets = parser.extractDatasets(xml);

        // 2) query for files to each index separately
        int numFiles = 0; // total number of files found
        for (final String index_node : datasets.keySet()) {
            
            HttpClient httpClient = new HttpClient();
            httpClient.setConnectionTimeout(CONNECTION_TIMEOUT);
            httpClient.setReadTimeout(READ_TIMEOUT);
            
            URL url = new URL("http://"+index_node+SEARCH_URI);
            
            // build "id="... multi-value constraint
            String data = QueryParameters.FIELD_TYPE+"="+QueryParameters.TYPE_FILE;
            List<String> ids = datasets.get(index_node); 
            for (String id : ids) {
                data += "&"+QueryParameters.FIELD_DATASET_ID+"="+URLEncoder.encode(id,"UTF-8");
            }
            //data += "&"+QueryParameters.LIMIT+"="+QueryParameters.DEFAULT_LIMIT;
            LOG.info("Querying URL="+url.toString()+ " for POST data="+data);
            String filesdoc = httpClient.doPost(url, data, false);
            
            // extract files from Solr response
            numFiles += parser.extractFiles(filesdoc, desc);
 
        }
                
        if (!response.isCommitted()) {
            
            // display message as plain text
            if (numFiles==0) {
                
                response.setContentType("text/plain");
                response.getWriter().print("No files were found that matched the query");
                
              
            // generate the wget script
            } else {
                // generate wget script
                final String wgetScript = WgetScriptGenerator.getWgetScript(desc);
                
                // write out the script to the HTTP response
                response.setContentType("text/x-sh");
                response.addHeader("Content-Disposition", "attachment; filename=" + String.format(SCRIPT_NAME, timestamp.format(new Date())) );
                response.setContentLength((int) wgetScript.length());
                PrintWriter out = response.getWriter();
                out.print(wgetScript);
            
            }

        }
        
    }
    
    private WgetScriptGenerator.WgetDescriptor getWgetDescriptor(HttpServletRequest request) {
        
        // initialize the wget script from the current request
        WgetScriptGenerator.init(request.getSession().getServletContext());
        
        // write out the URL + GET/POST parameters to the wget script
        StringBuilder parameters = new StringBuilder().append('?');
        @SuppressWarnings("unchecked")
        final Enumeration<String> e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            for (String value : request.getParameterValues(name)) {
                parameters.append(name).append('=').append(value).append('&');
            }
        }
        //there's always one more. either '?' if empty or '&' if not.
        parameters.setLength(parameters.length()-1);
        WgetScriptGenerator.WgetDescriptor desc = new WgetScriptGenerator.WgetDescriptor(
                request.getServerName(), null, 
                request.getRequestURL().toString() + parameters.toString());

        return desc;
        
    }

}
