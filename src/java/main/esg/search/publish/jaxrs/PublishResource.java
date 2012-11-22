package esg.search.publish.jaxrs;

import java.net.URL;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import edu.emory.mathcs.backport.java.util.Arrays;
import esg.search.publish.impl.solr.SolrClient;

@Path("/")
@Produces("application/xml")
public class PublishResource {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    // client that sends XML requests to the Solr server
    public SolrClient solrClient; 
        
    /**
     * Constructor is configured to interact with a specific Solr server.
     * @param url
     */
    @Autowired
    public PublishResource(final @Value("${esg.search.solr.publish.url}") URL url) {
        solrClient = new SolrClient(url);
    }
    
    @GET
    @Produces("text/plain")
    public String index() {
        return "ESGF REST Publishing Service";
    }
    
    @POST
    @Path("unpublish/")
    public String unpublish(@FormParam("id") List<String> ids) {
        
        if (LOG.isDebugEnabled()) {
            for (String id : ids) LOG.debug("Unpublishing id="+id);
        }
        
        try {
            String response = solrClient.delete( ids );
            return response;
            
        } catch(Exception e) {
            e.printStackTrace();
            throw newWebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
                
    }
    
    @POST
    @Path("publish/{type}/")
    public String publish(@PathParam("type") String type, String record) {
        
        if (LOG.isDebugEnabled()) LOG.debug("Publishing type="+type+" record="+record);
        try {
            String request = "<add>"+record+"</add>";
            String response = solrClient.index(request, type, true); // commit=true after this record
            return response;
        } catch(Exception e) {
            throw newWebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        
    }
    
    /**
     * Helper method to build an HTTP exception response with given body content and status code.
     * @param message
     * @param status
     * @return
     */
    private WebApplicationException newWebApplicationException(String message, Response.Status status) {
        
        ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.status(status);
        builder.entity("<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>"+message+"</error>");
        Response response = builder.build();
        return new WebApplicationException(response);
        
    }

}
