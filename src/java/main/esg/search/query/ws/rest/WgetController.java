package esg.search.query.ws.rest;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.emory.mathcs.backport.java.util.Arrays;
import esg.search.core.RecordHelper;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;
import esg.search.utils.XmlParser;

/**
 * Controller that returns a wget script that can be executed to retrieve all files matching the search criteria.
 * The allowed HTTP parameters are specified by the ESGF Search API, except for the fixed constraint "type=File".
 * 
 * @author Luca Cinquini
 *
 */
@Controller("wgetController")
public class WgetController {
    
    private static final String SCRIPT_NAME = "wget-%s.sh";
    private static final DateFormat timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
    @SuppressWarnings("unchecked")
    private static final Set<String> LOCAL_FIELDS = new HashSet<String>(Arrays.asList(new String[] {
            QueryParameters.FIELD_WGET_PATH,
            QueryParameters.FIELD_WGET_EMPTYPATH
    }));
    //to prevent facets values for extending too much
    private static final int MAX_DIR_LEGTH = 50; 
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    /**
     * The underlying base controller to which all calls are delegated.
     */
    final private BaseController baseController;
    
    @Autowired
    public WgetController(final BaseController baseController) {
          this.baseController = baseController;
    }
    
    /**
     * Method to process a search for files matching the given criteria,
     * and return a wget script.
     */
    @RequestMapping(value="/wget", method={ RequestMethod.GET, RequestMethod.POST })
    public void wget(final HttpServletRequest request, 
                       final SearchCommand command, 
                       final HttpServletResponse response) throws Exception {
    	
        HttpServletRequestWrapper new_req = new HttpServletRequestWrapper(request) {
            
            @Override
            public Enumeration<String> getParameterNames() {
                @SuppressWarnings("unchecked")
                Enumeration<String> e = super.getParameterNames();
                
                Vector<String> list = new Vector<String>();
                while (e.hasMoreElements()) {
                    String name = e.nextElement();
                    if (LOCAL_FIELDS.contains(name)) continue;
                    list.add(name);
                }
                return list.elements();
            }
            @Override
            public Map<String, String> getParameterMap() {
                @SuppressWarnings("unchecked")
                HashMap<String, String> new_map = new HashMap<String, String>(super.getParameterMap());
                for (String s : LOCAL_FIELDS) {
                    new_map.remove(s);
                }
                return new_map;
            }
            
            @Override
            public String getParameter(String name) {
                if (LOCAL_FIELDS.contains(name)) return null;
                return getRequest().getParameter(name);
            }
        };
        
        WgetScriptGenerator.init(request.getSession().getServletContext());
        
        // check type=... is not specified or properly specified to File
        if (request.getParameter(QueryParameters.FIELD_TYPE) != null &&
                request.getParameter(QueryParameters.FIELD_TYPE) != QueryParameters.TYPE_FILE) {
            baseController.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP parameter type is fixed to value: 'File'" 
                                     + request.getParameter(QueryParameters.FIELD_TYPE) + "'" +QueryParameters.TYPE_FILE , response);
            return;
        } else {
            command.setConstraint(QueryParameters.FIELD_TYPE, QueryParameters.TYPE_FILE);
        }
        
        //see if we have a directory structure to generate
        // set limit=MAX_LIMIT to enable massive retrieval through wget scripting, unless explicitly set already
        if (request.getParameter(QueryParameters.LIMIT) == null) command.setLimit(QueryParameters.MAX_LIMIT);
        
        String[] path = new String[0];
        if (request.getParameter(QueryParameters.FIELD_WGET_PATH) != null &&
                request.getParameter(QueryParameters.FIELD_WGET_PATH).length()>0) {
            path = request.getParameter(QueryParameters.FIELD_WGET_PATH).split(",");
            //command.removeConstraint(QueryParameters.FIELD_WGET_PATH);
        }
        //define what to do if a facet for a directory is not found
        String emptyPath;
        if (request.getParameter(QueryParameters.FIELD_WGET_EMPTYPATH) != null) {
            emptyPath = request.getParameter(QueryParameters.FIELD_WGET_EMPTYPATH);
            //command.removeConstraint(QueryParameters.FIELD_WGET_EMPTYPATH);
        } else {
            //defaults to "" which will skip that directory level.
            emptyPath = "";
        }
        
        // process request, obtain Solr/XML output
        String xml = baseController.process(new_req, command, response);
        
        // parse the Solr/XML document
        // build list of HTTPServer urls
        final XmlParser xmlParser = new XmlParser(false);
        final Document doc = xmlParser.parseString(xml);
        XPath xpath = XPath.newInstance("/response/result/doc");
        LOG.warn("***");
        LOG.warn(xml);

        
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
        
        //check we got all
        int res_count = ((Element) XPath.newInstance("/response/result").selectSingleNode(doc)
                ).getAttribute("numFound").getIntValue();
        int ret_count = xpath.selectNodes(doc).size();
            
        if (res_count> ret_count) {
            //this is just apart!
            desc.addMessage(String.format("Warning! The total number of files was " +
                    "%s but this script will only retrieve %s.", res_count, ret_count));                                    
        } else {
            desc.addMessage(String.format("Downloading %s files...\n", res_count));
        }
        
        // loop over records
        final String[] fieldsToExtract = new String[] {
                QueryParameters.FIELD_CHECKSUM,
                QueryParameters.FIELD_CHECKSUM_TYPE,
                QueryParameters.FIELD_SIZE, 
                QueryParameters.FIELD_URL };
        StringBuilder dir = new StringBuilder();
        for (Object obj : xpath.selectNodes(doc)) {
            dir.setLength(0);
            Element docEl = (Element)obj;
                        
            //prepare all attributes we need
            Map<String, String> attrib = new HashMap<String, String>();
            for (String s : fieldsToExtract)
                attrib.put(s, null);
            for (String s : path)
                attrib.put(s, emptyPath);

            //gather them
            for (final Object childObj : docEl.getChildren()) {
                Element childEl = (Element) childObj;
                if (attrib.containsKey(childEl.getAttributeValue("name"))) {
                    //Handle exceptions!
                    if (childEl.getAttributeValue("name")
                            .equals(QueryParameters.FIELD_URL)) {
                        String tuple = childEl.getChild("str")
                                .getTextNormalize();
                        String[] parts = RecordHelper.decodeTuple(tuple);
                        if (parts[2]
                                .equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_HTTP)) {
                            attrib.put(childEl.getAttributeValue("name"), parts[0]);
                        }
                        //handle all arrays
                    } else if (childEl.getName() == "arr"){
                        attrib.put(childEl.getAttributeValue("name"), childEl
                                .getChild("str").getTextNormalize());
                        //and the rest as usual
                    } else {
                        attrib.put(childEl.getAttributeValue("name"), childEl
                                   .getTextNormalize());                        
                    }
                }
            }
            
            
            for (String facet : path) {
                //prevent strange values while generating names as well as too long names
                String value = attrib.get(facet).replaceAll("['<>?*\"\n\t\r\0]", "").replaceAll("[ /\\\\|:;]+", "_");
                if (value.length() > MAX_DIR_LEGTH) {
                    value = value.substring(0, MAX_DIR_LEGTH);
                }
                dir.append(value).append('/');
            }
            desc.addFile(attrib.get(QueryParameters.FIELD_URL),
                         dir.toString(), attrib.get(QueryParameters.FIELD_SIZE), 
                         attrib.get(QueryParameters.FIELD_CHECKSUM_TYPE),
                         attrib.get(QueryParameters.FIELD_CHECKSUM));
            
        }       
        
        if (!response.isCommitted()) {
            
            // display message as plain text
            if (res_count==0) {
                
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

}
