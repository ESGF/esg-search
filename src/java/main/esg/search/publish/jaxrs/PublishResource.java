package esg.search.publish.jaxrs;

import java.net.URL;

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
    @Produces("text/plain")
    public void unpublish(@FormParam("id") String[] ids) {
        
        if (LOG.isDebugEnabled()) {
            for (String id : ids) LOG.debug("Unpublishing id="+id);
        }
        
        try {
            solrClient.delete( Arrays.asList( ids ) );
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
                
    }
    
    @POST
    @Path("publish/{type}/")
    public String publish(@PathParam("type") String type, String record) {
        
        if (LOG.isDebugEnabled()) LOG.debug("Publishing type="+type+" record="+record);
        
        String request = "<add>"+record+"</add>";
        
        try {
            String response = solrClient.index(request, type, true); // commit=true after this record
            return response;
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
    }

}
