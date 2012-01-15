package esg.search.query.ws.rest;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import esg.search.core.RecordHelper;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.XmlParser;

/**
 * Controller that returns a wget script that can be executed to retrieve all files matching the search criteria.
 * The allowed HTTP parameters are specified by the ESGF Search API, except for the fixed constraint "type=File".
 * 
 * @author Luca Cinquini
 *
 */
@Controller("wgetControllerOld")
public class WgetControllerOld {
    
    private static final String SCRIPT_NAME = "wget.sh";
    
    /**
     * The underlying base controller to which all calls are delegated.
     */
    final private BaseController baseController;
    
    @Autowired
    public WgetControllerOld(final BaseController baseController) {
          this.baseController = baseController;
    }
    
    /**
     * Method to process a search for files matching the given criteria,
     * and return a wget script.
     */
    @RequestMapping(value="/wgetOld", method={ RequestMethod.GET, RequestMethod.POST })
    public void wget(final HttpServletRequest request, 
                       final SearchCommand command, 
                       final HttpServletResponse response) throws Exception {
        
        // check type=... is not specified
        if (request.getParameter(QueryParameters.FIELD_TYPE)!=null) {
            baseController.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP parameter type is fixed to value: File", response);
            return;
        } else {
            command.setConstraint(QueryParameters.FIELD_TYPE, SolrXmlPars.TYPE_FILE);
        }
        
        // process request, obtain Solr/XML output
        String xml = baseController.process(request, command, response);
        
        // parse the Solr/XML document
        // build list of HTTPServer urls
        final XmlParser xmlParser = new XmlParser(false);
        final Document doc = xmlParser.parseString(xml);
        //XPath xpath = XPath.newInstance("/response/result/doc/arr[@name='url']/str");
        XPath xpath = XPath.newInstance("/response/result/doc");
        
        final List<String> urls = new ArrayList<String>();
        // loop over records
        for (Object obj : xpath.selectNodes(doc)) {
            Element docEl = (Element)obj;
                        
            String url = ""; // Note: only extract one URL for each record
            String checksum = "";
            String checksumType = "";

            for (final Object childObj : docEl.getChildren("arr")) {
                
                Element childEl = (Element)childObj;
                
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_CHECKSUM)) {
                    checksum = childEl.getChild("str").getTextNormalize();
                }
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_CHECKSUM_TYPE)) {
                    checksumType = childEl.getChild("str").getTextNormalize();
                }
                if (childEl.getAttributeValue("name").equals(QueryParameters.FIELD_URL)) {
                    String tuple = childEl.getChild("str").getTextNormalize();
                    String[] parts = RecordHelper.decodeTuple(tuple);
                    if (parts[2].equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_HTTP)) {
                        url = parts[0];
                    }
                }
             }
            // export (URL checkum) for this record
            if (StringUtils.hasText(url)) {
                urls.add( url );
                //urls.add( url + (StringUtils.hasText(checksum) ? " "+checksum : ""));
            }
            
        }       
        
        if (!response.isCommitted()) {
            
            // display message as plain text
            if (urls.size()==0) {
                
                response.setContentType("text/plain");
                response.getWriter().print("No files were found that matched the query");
                
              
            // generate the wget script
            } else {
            
                // record the full request URL
                final StringBuffer requestUrl = request.getRequestURL();
                if (StringUtils.hasText(request.getQueryString())) requestUrl.append("?").append(request.getQueryString());
                
                // generate wget script
                final String wgetScript = WgetScriptGeneratorOld.createWgetScript(requestUrl.toString(), urls);
                
                // write out the script to the HTTP response
                response.setContentType("text/x-sh");
                response.addHeader("Content-Disposition", "attachment; filename=" + SCRIPT_NAME );
                response.setContentLength((int) wgetScript.length());
                PrintWriter out = response.getWriter();
                out.print(wgetScript);
            
            }

        }
        
    }

}
