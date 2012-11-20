package esg.search.publish.jaxrs;

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

import edu.emory.mathcs.backport.java.util.Arrays;
import esg.search.publish.impl.solr.SolrIndexer;
import esg.search.publish.impl.solr.SolrScrabber;

@Path("/")
@Produces("application/xml")
public class PublishResource {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    @Autowired
    public SolrIndexer solrIndexer;
    
    @Autowired
    public SolrScrabber solrScrabber;
    
    public PublishResource() {}
    
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
            solrScrabber.delete( Arrays.asList( ids ) );
        } catch(Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
                
    }
    
    @POST
    @Path("publish/{type}/")
    public String publish(@PathParam("type") String type, String record) {
        
        if (LOG.isDebugEnabled()) LOG.debug("Publishing type="+type+" record="+record);
        
        String request = "<add>"+record+"</add>";
        
        try {
            String response = solrIndexer.index(request, type, true); // commit=true after this record
            return response;
        } catch(Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
    }

}
